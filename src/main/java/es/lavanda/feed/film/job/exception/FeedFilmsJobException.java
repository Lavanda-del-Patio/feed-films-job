package es.lavanda.feed.film.job.exception;

public class FeedFilmsJobException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public FeedFilmsJobException(String message) {
        super(message);
    }

    public FeedFilmsJobException(String message, Exception e) {
        super(message, e);
    }
}