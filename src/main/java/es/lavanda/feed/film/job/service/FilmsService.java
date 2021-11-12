package es.lavanda.feed.film.job.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import es.lavanda.feed.film.job.model.FilmModel;
import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaODTO;

public interface FilmsService {

    void executeFilm(FilmModelTorrent filmModelTorrent);

}
