package bg.nbu.medicalrecords.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Value;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for {@link bg.nbu.medicalrecords.domain.SickLeave}
 */
@Value
@JsonIgnoreProperties(ignoreUnknown = true)
public class SickLeaveDto implements Serializable {

    Long id;

    @NotBlank(message = "Reason cannot be blank")
    String reason;

    @NotNull(message = "Today's date cannot be null")
    @FutureOrPresent(message = "Today's date must be today or in the future")
    LocalDate todayDate;

    @NotNull(message = "Start date cannot be null")
    @FutureOrPresent(message = "Start date must be today or in the future")
    LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    @FutureOrPresent(message = "End date must be today or in the future")
    LocalDate endDate;

    LocalDateTime createdAt;

    LocalDateTime updatedAt;
}
