package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Appointment}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAppointmentDto implements Serializable {
    Long doctorId;
    LocalDateTime appointmentDateTime;
}