package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UpdatePatientDto {

    private String name;

    private String egn;

    @NotNull
    private Boolean healthInsurancePaid;

    private Long primaryDoctorId;

}
