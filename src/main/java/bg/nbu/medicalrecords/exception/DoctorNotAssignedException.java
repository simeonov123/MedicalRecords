package bg.nbu.medicalrecords.exception;

public class DoctorNotAssignedException extends RuntimeException {
    public DoctorNotAssignedException(String message) {
        super(message);
    }
}