package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateDiagnosisDto {
    @NotBlank(message = "Diagnosis statement cannot be blank")
    private String statement;

    @NotNull(message = "Diagnosed date cannot be null")
    private LocalDateTime diagnosedDate;
}