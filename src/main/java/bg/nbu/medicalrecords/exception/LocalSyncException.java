package bg.nbu.medicalrecords.exception;

public class LocalSyncException extends RuntimeException {
    public LocalSyncException(String message) {
        super(message);
    }

    public LocalSyncException(String message, Throwable cause) {
        super(message, cause);
    }
}