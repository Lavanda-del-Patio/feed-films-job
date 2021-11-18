package es.lavanda.feed.film.job.service;

import java.util.List;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;

import es.lavanda.feed.film.job.exception.FeedFilmsJobException;
import es.lavanda.feed.film.job.model.LambdaDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskService implements CommandLineRunner {

    private FilmsService filmsServiceImpl;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    private static AmazonSQS amazonSQS;

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("https://sqs.eu-west-1.amazonaws.com/836783797541/feed-films-${spring.profiles.active}")
    private String queueUrl;

    @Override
    public void run(String... args) throws Exception {
        amazonSQS = AmazonSQSClientBuilder.standard().withRegion(awsRegion).build();
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withMaxNumberOfMessages(1).withWaitTimeSeconds(3);
        final List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
        for (Message messageObject : messages) {
            String message = messageObject.getBody();
            log.info("Received message: " + message);
            ObjectMapper mapper = new ObjectMapper();
            LambdaDTO lambda = new LambdaDTO();
            try {
                lambda = mapper.readValue(message, LambdaDTO.class);
            } catch (JsonProcessingException e) {
                log.error("The message cannot convert to FilmModelTorrent", e);
                throw new FeedFilmsJobException("The message cannot convert to        FilmModelTorrent", e);
            }
            lambda.getFilmModelTorrents().forEach(filmsServiceImpl::executeFilm);
            log.info("Finish task, proceeding to delete message on queue");
            deleteMessage(messageObject);
            rabbitTemplate.stop();
        }
    }

    private void deleteMessage(Message messageObject) {
        log.info("Deleting message");
        final String messageReceiptHandle = messageObject.getReceiptHandle();
        amazonSQS.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
        log.info("Deleted");

    }
}