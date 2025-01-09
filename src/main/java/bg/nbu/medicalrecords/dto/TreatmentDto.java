package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Treatment}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class TreatmentDto implements Serializable {
    Long id;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LocalDate startDate;
    LocalDate endDate;
    List<PrescriptionDto> prescriptions;
     String description;

}