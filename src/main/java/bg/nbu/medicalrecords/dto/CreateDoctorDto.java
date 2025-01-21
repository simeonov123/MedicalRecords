package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateDoctorDto {
    @NotBlank(message = "Unique identifier cannot be blank")
    private String uniqueIdentifier;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    private String specialties;

    @NotNull(message = "Primary care status cannot be null")
    private Boolean primaryCare; // Cannot be null in the database

}
