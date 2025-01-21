// SickLeaveNotFoundException.java
package bg.nbu.medicalrecords.exception;

public class SickLeaveNotFoundException extends RuntimeException {
    public SickLeaveNotFoundException(String message) {
        super(message);
    }
}