package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateDoctorDto {
    @NotBlank
    private String uniqueIdentifier;

    @NotBlank
    private String name;

    private String specialties;
    private Boolean primaryCare; // cannot be null in DB, ensure passed

}
