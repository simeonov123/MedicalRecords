package bg.nbu.medicalrecords.dto;


import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DiagnosisDetailsDto {
        String statement;
        Long count;
        Long percentageOfAllDiagnoses;
        Long percentageOfAllPatients;
        String doctorNameOfFirstDiagnosis;
        LocalDateTime dateOfFirstDiagnosis;
        LocalDateTime dateOfLastDiagnosis;
}
