package es.lavanda.feed.film.job.config;


import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.cloud.aws.autoconfigure.context.ContextInstanceDataAutoConfiguration;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@Configuration
@EnableMongoAuditing
@EnableAutoConfiguration(exclude = { ContextInstanceDataAutoConfiguration.class })
public class AppConfig {

}
