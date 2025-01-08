package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Prescription}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class PrescriptionDto implements Serializable {
    Long id;
    MedicationDto medication;
    String dosage;
    Integer duration;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}