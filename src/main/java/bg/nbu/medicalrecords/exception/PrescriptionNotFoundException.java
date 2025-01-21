// PrescriptionNotFoundException.java
package bg.nbu.medicalrecords.exception;

public class PrescriptionNotFoundException extends RuntimeException {
    public PrescriptionNotFoundException(String message) {
        super(message);
    }
}