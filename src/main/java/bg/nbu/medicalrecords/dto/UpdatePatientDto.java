package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePatientDto {

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Pattern(regexp = "\\d{10}", message = "EGN must be exactly 10 digits")
    private String egn;

    @NotNull(message = "Health insurance status must be specified")
    private Boolean healthInsurancePaid;

    private Long primaryDoctorId; // Can be null, so no validation
}
