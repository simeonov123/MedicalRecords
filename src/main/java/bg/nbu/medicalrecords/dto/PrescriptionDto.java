package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Prescription}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescriptionDto implements Serializable {

    @NotNull(message = "ID cannot be null")
    Long id;

    @NotNull(message = "Medication cannot be null")
    @Valid
    MedicationDto medication;

    @NotBlank(message = "Dosage cannot be blank")
    String dosage;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 day")
    Integer duration;

    @NotNull(message = "Creation date cannot be null")
    LocalDateTime createdAt;

    @NotNull(message = "Updated date cannot be null")
    LocalDateTime updatedAt;
}
