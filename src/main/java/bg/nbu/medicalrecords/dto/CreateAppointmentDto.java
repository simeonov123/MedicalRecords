package bg.nbu.medicalrecords.dto;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * For creating an appointment.
 */
@Data
public class CreateAppointmentDto {
    private Long patientId;
    private Long doctorId;
    private LocalDateTime date;
}
