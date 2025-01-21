package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Appointment}
 */
@Data
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateAppointmentDto implements Serializable {

    @NotNull(message = "Doctor ID cannot be null")
    Long doctorId;

    @NotNull(message = "Appointment date and time cannot be null")
    LocalDateTime appointmentDateTime;
}
