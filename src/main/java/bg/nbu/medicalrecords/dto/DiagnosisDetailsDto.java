package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisDetailsDto {

        @NotBlank(message = "Statement cannot be blank")
        private String statement;

        @NotNull(message = "Count cannot be null")
        @Min(value = 0, message = "Count must be zero or a positive number")
        private Long count;

        @NotNull(message = "Percentage of all diagnoses cannot be null")
        @Min(value = 0, message = "Percentage of all diagnoses must be zero or a positive number")
        private Long percentageOfAllDiagnoses;

        @NotNull(message = "Percentage of all patients cannot be null")
        @Min(value = 0, message = "Percentage of all patients must be zero or a positive number")
        private Long percentageOfAllPatients;

        @NotBlank(message = "Doctor name of first diagnosis cannot be blank")
        private String doctorNameOfFirstDiagnosis;

        @NotNull(message = "Date of first diagnosis cannot be null")
        private LocalDateTime dateOfFirstDiagnosis;

        @NotNull(message = "Date of last diagnosis cannot be null")
        private LocalDateTime dateOfLastDiagnosis;
}
