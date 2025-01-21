// DoctorNotFoundException.java
package bg.nbu.medicalrecords.exception;

public class DoctorNotFoundException extends RuntimeException {
    public DoctorNotFoundException(String message) {
        super(message);
    }
}