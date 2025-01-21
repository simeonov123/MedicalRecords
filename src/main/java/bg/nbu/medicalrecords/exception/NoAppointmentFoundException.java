package bg.nbu.medicalrecords.exception;

public class NoAppointmentFoundException extends RuntimeException {
    public NoAppointmentFoundException(String message) {
        super(message);
    }
}