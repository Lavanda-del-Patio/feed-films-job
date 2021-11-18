package es.lavanda.feed.film.job.service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import es.lavanda.feed.film.job.exception.FeedFilmsJobException;
import es.lavanda.feed.film.job.model.FilmModel;
import es.lavanda.feed.film.job.repository.FilmModelRepository;
import es.lavanda.lib.common.model.FilmModelTorrent;
import es.lavanda.lib.common.model.MediaIDTO;
import es.lavanda.lib.common.model.MediaIDTO.Type;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmsServiceImpl implements FilmsService {

    private final FilmModelRepository filmModelRepository;

    private final ProducerService producerService;

    @Override
    public void executeFilm(FilmModelTorrent filmModelTorrent) {
        log.info("Execute film {}", filmModelTorrent.toString());
        if (Boolean.FALSE.equals(filmModelRepository.existsByTorrentsTorrentUrl(filmModelTorrent.getTorrentUrl()))) {
            try {
                log.info("Torrent {} no exist on database ", filmModelTorrent.getTorrentUrl());
                createNewFilmModel(List.of(filmModelTorrent));
            } catch (FeedFilmsJobException e) {
                log.error("Not sended torrent to agent", e);
                throw e;
            }
        }
    }

    private void createNewFilmModel(List<FilmModelTorrent> filmModelTorrents) {
        if (Boolean.FALSE.equals(filmModelTorrents.isEmpty())) {
            log.info("Creating new filmModel with this data {} ", filmModelTorrents.get(0).getTorrentTitle());
            FilmModel filmModel = new FilmModel();
            filmModel.setTorrents(new HashSet<>(filmModelTorrents));
            sendToAgent(save(filmModel));
        }
    }

    private void sendToAgent(FilmModel filmModel) {
        log.info("Sending to agent the filmModel with ID {}", filmModel.getId());
        MediaIDTO mediaIDTO = new MediaIDTO();
        mediaIDTO.setId(filmModel.getId());
        String torrentCroppedTitle = filmModel.getTorrents().stream()
                .filter(torrent -> StringUtils.hasText(torrent.getTorrentCroppedTitle()))
                .map(FilmModelTorrent::getTorrentCroppedTitle).findFirst().orElse(null);
        if (Objects.nonNull(torrentCroppedTitle)) {
            mediaIDTO.setPossibleType(getPossibleType(torrentCroppedTitle));
        }
        mediaIDTO.setTorrentCroppedTitle(torrentCroppedTitle);
        mediaIDTO.setTorrentTitle(
                filmModel.getTorrents().stream().filter(torrent -> StringUtils.hasText(torrent.getTorrentTitle()))
                        .map(FilmModelTorrent::getTorrentTitle).findFirst().orElse(null));
        mediaIDTO.setTorrentYear(filmModel.getTorrents().stream().filter(torrent -> torrent.getTorrentYear() >= 0)
                .map(FilmModelTorrent::getTorrentYear).findFirst().orElse(null));
        if (Objects.nonNull(filmModel.getType())) {
            mediaIDTO.setType(filmModel.getType());
        }
        producerService.sendToFeedAgentTMDB(mediaIDTO);
    }

    private Type getPossibleType(String torrentCroppedTitle) {
        return torrentCroppedTitle.contains("Temporada") ? Type.SHOW : Type.FILM;
    }

    private FilmModel save(FilmModel filmModel) {
        return filmModelRepository.save(filmModel);
    }

}