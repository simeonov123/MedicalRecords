package bg.nbu.medicalrecords.dto;

import lombok.Data;
import java.time.LocalDate;

/**
 * For creating/updating an appointment.
 */
@Data
public class CreateAppointmentDto {
    private Long patientId;
    private Long doctorId;
    private Long diagnosisId;
    private String treatment;
    private Integer sickLeaveDays; // optional
    private LocalDate date;
}
