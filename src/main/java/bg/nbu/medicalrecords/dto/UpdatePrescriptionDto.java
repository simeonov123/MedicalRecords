package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Prescription}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePrescriptionDto implements Serializable {

    @NotNull(message = "Medication ID cannot be null")
    Long medicationId;

    @NotBlank(message = "Dosage cannot be blank")
    String dosage;

    @NotNull(message = "Duration cannot be null")
    @Min(value = 1, message = "Duration must be at least 1 day")
    Integer duration;
}
