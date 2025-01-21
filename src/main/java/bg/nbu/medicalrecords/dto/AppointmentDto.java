package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Appointment}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class AppointmentDto implements Serializable {

    @NotNull(message = "ID cannot be null")
    Long id;

    @NotNull(message = "Patient cannot be null")
    @Valid
    PatientDto patient;

    @NotNull(message = "Doctor cannot be null")
    @Valid
    DoctorDto doctor;

    @Size(min = 1, message = "At least one diagnosis is required")
    @Valid
    List<DiagnosisDto> diagnoses;

    @Valid
    List<SickLeaveDto> sickLeaves;

    @NotNull(message = "Creation date cannot be null")
    LocalDateTime createdAt;

    @NotNull(message = "Updated date cannot be null")
    LocalDateTime updatedAt;

    @NotNull(message = "Appointment date and time cannot be null")
    @FutureOrPresent(message = "Appointment date and time must be in the future or present")
    LocalDateTime appointmentDateTime;
}
