package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Prescription}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdatePrescriptionDto implements Serializable {
    Long medicationId;
    String dosage;
    Integer duration;
}