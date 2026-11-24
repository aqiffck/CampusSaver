package campussaver.exception;

/** Thrown when live exchange rates cannot be fetched or read. Callers fall back to rates.txt. */
public class RateFetchException extends Exception {
    public RateFetchException(String message) {
        super(message);
    }
}