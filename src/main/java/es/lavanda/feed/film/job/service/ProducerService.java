package es.lavanda.feed.film.job.service;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import es.lavanda.feed.film.job.exception.FeedFilmsJobException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class ProducerService {

    private final RabbitTemplate rabbitTemplate;

    public void sendToFeedAgentTMDB(Object message) throws FeedFilmsJobException {
        try {
            log.info("Sending message to queue agent-tmdb-feed-films");
            rabbitTemplate.convertAndSend("agent-tmdb-feed-films", message);
            log.info("Sended message to queue agent-tmdb-feed-films {}", message);
        } catch (Exception e) {
            log.error("Failed send message to queue agent-tmdb-feed-films", e);
            throw new FeedFilmsJobException("Failed send message to queue agent-tmdb-feed-films", e);
        }
    }
}
