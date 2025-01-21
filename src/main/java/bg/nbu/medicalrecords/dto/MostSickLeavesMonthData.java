package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class MostSickLeavesMonthData {

    @NotBlank(message = "Month name cannot be blank")
    private String monthName;

    @NotNull(message = "Sick leaves count cannot be null")
    @Min(value = 0, message = "Sick leaves count must be zero or a positive number")
    private Integer sickLeavesCount;

    @NotNull(message = "Appointments count cannot be null")
    @Min(value = 0, message = "Appointments count must be zero or a positive number")
    private Integer appointmentsThatMonthCount;

    @NotNull(message = "Unique patients count cannot be null")
    @Min(value = 0, message = "Unique patients count must be zero or a positive number")
    private Integer uniquePatientsCount;

    @NotBlank(message = "Most common diagnosis cannot be blank")
    private String mostCommonDiagnosisThatMonth;
}
