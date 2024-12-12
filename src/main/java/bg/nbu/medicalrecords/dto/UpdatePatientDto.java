package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePatientDto {
    @NotBlank
    private String name;

    @NotBlank
    private String egn;

    @NotNull
    private Boolean healthInsurancePaid;

    private Long primaryDoctorId;

}
