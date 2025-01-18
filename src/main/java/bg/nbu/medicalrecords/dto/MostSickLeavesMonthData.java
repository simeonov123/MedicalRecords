package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class MostSickLeavesMonthData {
    private String monthName;
    private int sickLeavesCount;
    private int appointmentsThatMonthCount;
    private int uniquePatientsCount;
    private String mostCommonDiagnosisThatMonth;


}
