package bg.nbu.medicalrecords.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UpdateSickLeaveDto {
    String reason;
    LocalDate todayDate;
    LocalDate startDate;
    LocalDate endDate;
}
