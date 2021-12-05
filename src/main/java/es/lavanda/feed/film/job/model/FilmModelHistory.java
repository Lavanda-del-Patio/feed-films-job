package es.lavanda.feed.film.job.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document("feed_films_history")
public class FilmModelHistory {

    @Id
    private String id;

    private String torrentUrl;

}
