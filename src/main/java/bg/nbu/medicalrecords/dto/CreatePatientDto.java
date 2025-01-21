package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreatePatientDto {

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @NotBlank(message = "EGN cannot be blank")
    @Pattern(regexp = "\\d{10}", message = "EGN must be exactly 10 digits")
    private String egn;

    @NotNull(message = "Health insurance status must be specified")
    private Boolean healthInsurancePaid;

    private Long primaryDoctorId; // Can be null
}
