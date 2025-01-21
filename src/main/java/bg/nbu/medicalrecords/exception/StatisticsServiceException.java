package bg.nbu.medicalrecords.exception;

public class StatisticsServiceException extends RuntimeException {
    public StatisticsServiceException(String message) {
        super(message);
    }

    public StatisticsServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}