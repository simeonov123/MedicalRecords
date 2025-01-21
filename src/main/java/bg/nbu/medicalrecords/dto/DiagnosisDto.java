package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "ID cannot be null")
    Long id;

    @NotBlank(message = "Statement cannot be blank")
    String statement;

    @NotNull(message = "Diagnosed date cannot be null")
    LocalDateTime diagnosedDate;

    @NotNull(message = "Creation date cannot be null")
    LocalDateTime createdAt;

    @NotNull(message = "Updated date cannot be null")
    LocalDateTime updatedAt;

    @Valid
    List<TreatmentDto> treatments;
}
