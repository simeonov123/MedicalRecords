package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdateDoctorDto {
    @NotBlank
    private String uniqueIdentifier;

    @NotBlank
    private String name;

    private String specialties;

    @NotNull
    private Boolean primaryCare;


}
