package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DoctorsSickLeavesLeaderboardDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Specialties cannot exceed 500 characters")
    private String specialties;

    private boolean primaryCare; // No validation needed as a primitive type cannot be null

    @NotNull(message = "Sick leaves count cannot be null")
    @Min(value = 0, message = "Sick leaves count must be zero or a positive number")
    private Integer sickLeavesCount;
}
