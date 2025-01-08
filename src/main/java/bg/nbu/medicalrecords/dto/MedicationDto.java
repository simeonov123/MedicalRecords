package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Medication}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class MedicationDto implements Serializable {
    Long id;
    String medicationName;
    String dosageForm;
    String strength;
    String sideEffect;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}