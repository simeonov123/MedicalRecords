package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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

    @NotNull(message = "ID cannot be null")
    Long id;

    @NotNull(message = "Creation date cannot be null")
    LocalDateTime createdAt;

    @NotNull(message = "Updated date cannot be null")
    LocalDateTime updatedAt;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be today or in the future")
    LocalDate endDate;

    @Valid
    List<PrescriptionDto> prescriptions;

    @NotBlank(message = "Description cannot be blank")
    String description;
}
