// MedicationNotFoundException.java
package bg.nbu.medicalrecords.exception;

public class MedicationNotFoundException extends RuntimeException {
    public MedicationNotFoundException(String message) {
        super(message);
    }
}