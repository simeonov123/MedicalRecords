package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Medication}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicationDto implements Serializable {

    @NotNull(message = "ID cannot be null")
    Long id;

    @NotBlank(message = "Medication name cannot be blank")
    @Size(max = 255, message = "Medication name cannot exceed 255 characters")
    String medicationName;

    @NotBlank(message = "Dosage form cannot be blank")
    @Size(max = 100, message = "Dosage form cannot exceed 100 characters")
    String dosageForm;

    @NotBlank(message = "Strength cannot be blank")
    @Size(max = 100, message = "Strength cannot exceed 100 characters")
    String strength;

    @Size(max = 500, message = "Side effects cannot exceed 500 characters")
    String sideEffect;

    @NotNull(message = "Created at cannot be null")
    LocalDateTime createdAt;

    @NotNull(message = "Updated at cannot be null")
    LocalDateTime updatedAt;
}
