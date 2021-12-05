package es.lavanda.feed.film.job.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import es.lavanda.feed.film.job.model.FilmModelHistory;

public interface FilmModelHistoryRepository extends MongoRepository<FilmModelHistory, String> {

    boolean existsByTorrentUrl(String torrentUrl);

}
