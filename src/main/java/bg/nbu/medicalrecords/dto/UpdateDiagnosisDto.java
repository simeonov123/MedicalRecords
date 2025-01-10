package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Diagnosis}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDiagnosisDto implements Serializable {
    String statement;
    LocalDateTime diagnosedDate;
}