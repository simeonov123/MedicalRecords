package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DoctorPatientCountDto {

    @NotBlank(message = "Doctor name cannot be blank")
    private String doctorName;

    @NotNull(message = "Count cannot be null")
    @Min(value = 0, message = "Count must be zero or a positive number")
    private Long count;
}
