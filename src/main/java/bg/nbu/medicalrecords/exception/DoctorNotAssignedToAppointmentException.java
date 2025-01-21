// DoctorNotAssignedToAppointmentException.java
package bg.nbu.medicalrecords.exception;

public class DoctorNotAssignedToAppointmentException extends RuntimeException {
    public DoctorNotAssignedToAppointmentException(String message) {
        super(message);
    }
}