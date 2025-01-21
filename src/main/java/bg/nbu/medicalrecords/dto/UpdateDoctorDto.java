package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateDoctorDto {

    @NotBlank(message = "Unique identifier cannot be blank")
    @Size(max = 50, message = "Unique identifier cannot exceed 50 characters")
    private String uniqueIdentifier;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 100, message = "Name cannot exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Specialties cannot exceed 500 characters")
    private String specialties;

    @NotNull(message = "Primary care status cannot be null")
    private Boolean primaryCare;
}
