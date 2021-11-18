package es.lavanda.feed.film.job.service;

import es.lavanda.lib.common.model.FilmModelTorrent;

public interface FilmsService {

    void executeFilm(FilmModelTorrent filmModelTorrent);

}
