package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.Diagnosis}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class UpdateDiagnosisDto implements Serializable {

    @NotBlank(message = "Statement cannot be blank")
    String statement;

    @NotNull(message = "Diagnosed date cannot be null")
    LocalDateTime diagnosedDate;
}
