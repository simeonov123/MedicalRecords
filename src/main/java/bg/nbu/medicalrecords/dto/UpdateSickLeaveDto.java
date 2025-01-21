package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSickLeaveDto {

    @NotBlank(message = "Reason cannot be blank")
    private String reason;

    @NotNull(message = "Today's date cannot be null")
    @FutureOrPresent(message = "Today's date must be today or in the future")
    private LocalDate todayDate;

    @NotNull(message = "Start date cannot be null")
    private LocalDate startDate;

    @NotNull(message = "End date cannot be null")
    private LocalDate endDate;
}
