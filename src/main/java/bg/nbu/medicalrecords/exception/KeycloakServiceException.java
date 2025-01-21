package bg.nbu.medicalrecords.exception;

public class KeycloakServiceException extends RuntimeException {
    public KeycloakServiceException(String message) {
        super(message);
    }
}