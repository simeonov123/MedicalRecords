package bg.nbu.medicalrecords.dto;

import lombok.Data;

import java.util.List;

@Data
public class DiagnosisStatisticsDto {
    List<DiagnosisDetailsDto> diagnosisDetails;

}
