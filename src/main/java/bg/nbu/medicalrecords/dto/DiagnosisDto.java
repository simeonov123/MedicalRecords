package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Diagnosis}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class DiagnosisDto implements Serializable {
    Long id;
    String statement;
    LocalDateTime diagnosedDate;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    List<TreatmentDto> treatments;
}