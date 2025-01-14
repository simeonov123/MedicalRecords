package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Treatment}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateTreatmentDto implements Serializable {
    LocalDate startDate;
    LocalDate endDate;
    String description;
}