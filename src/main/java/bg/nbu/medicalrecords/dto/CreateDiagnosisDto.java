package bg.nbu.medicalrecords.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CreateDiagnosisDto {
    private String statement;
    private LocalDateTime diagnosedDate;
}