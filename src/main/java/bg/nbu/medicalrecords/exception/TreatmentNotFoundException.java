// TreatmentNotFoundException.java
package bg.nbu.medicalrecords.exception;

public class TreatmentNotFoundException extends RuntimeException {
    public TreatmentNotFoundException(String message) {
        super(message);
    }
}