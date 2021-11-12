package es.lavanda.feed.film.job.service;

import java.util.List;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSClientBuilder;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;

import es.lavanda.feed.film.job.exception.FeedFilmsJobException;
import es.lavanda.feed.film.job.model.LambdaDTO;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class TaskService implements CommandLineRunner {

    @Autowired
    private FilmsService filmsServiceImpl;

    private static AmazonSQS amazonSQS;

    @Value("${cloud.aws.region.static}")
    private String awsRegion;

    @Value("https://sqs.eu-west-1.amazonaws.com/836783797541/feed-films-${spring.profiles.active}")
    private String queueUrl;

    // @Override
    // public AmazonSQSAsync amazonSQSAsync() {
    // return AmazonSQSAsyncClientBuilder.standard().withRegion(awsRegion).build();
    // }

    // @Bean
    // public QueueMessagingTemplate queueMessagingTemplate(AmazonSQSAsync
    // amazonSQSAsync) {
    // return new QueueMessagingTemplate(amazonSQSAsync());
    // }

    // @Bean
    // public SimpleMessageListenerContainerFactory
    // simpleMessageListenerContainerFactory() {
    // SimpleMessageListenerContainerFactory factory = new
    // SimpleMessageListenerContainerFactory();
    // factory.setAmazonSqs(amazonSQSAsync());
    // factory.setMaxNumberOfMessages(1);
    // factory.setQueueMessageHandler(new QueueMessageHandler());
    // factory.setWaitTimeOut(20);
    // return factory;
    // }
    @Override
    public void run(String... args) throws Exception {
        // AWSCredentialsProvider awsCredentialsProvider = new
        // AWSStaticCredentialsProvider(
        // new BasicAWSCredentials());
        amazonSQS = AmazonSQSClientBuilder.standard().withRegion(awsRegion).build();
        final ReceiveMessageRequest receiveMessageRequest = new ReceiveMessageRequest(queueUrl)
                .withMaxNumberOfMessages(1).withWaitTimeSeconds(3);
        final List<Message> messages = amazonSQS.receiveMessage(receiveMessageRequest).getMessages();
        for (Message messageObject : messages) {
            String message = messageObject.getBody();
            ObjectMapper mapper = new ObjectMapper();
            LambdaDTO lambda = new LambdaDTO();
            try {
                lambda = mapper.readValue(message, LambdaDTO.class);
            } catch (JsonProcessingException e) {
                log.error("The message cannot convert to FilmModelTorrent", e);
                throw new FeedFilmsJobException("The message cannot convert to        FilmModelTorrent", e);
            }
            lambda.getFilmModelTorrents().forEach(filmsServiceImpl::executeFilm);
            log.debug("Work message finished");
            log.info("Hello, World!");
            log.info("Received message: " + message);
            deleteMessage(messageObject);
        }

    }

    private void deleteMessage(Message messageObject) {
        final String messageReceiptHandle = messageObject.getReceiptHandle();
        amazonSQS.deleteMessage(new DeleteMessageRequest(queueUrl, messageReceiptHandle));
    }
}