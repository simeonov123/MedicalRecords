package bg.nbu.medicalrecords.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class DiagnosisStatisticsDto {

    @NotEmpty(message = "Diagnosis details list cannot be empty")
    @Valid
    private List<DiagnosisDetailsDto> diagnosisDetails;
}
