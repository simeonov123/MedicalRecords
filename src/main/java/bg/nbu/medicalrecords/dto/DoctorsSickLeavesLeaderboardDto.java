package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class DoctorsSickLeavesLeaderboardDto {
    private String name;
    private String specialties;
    private boolean primaryCare;
    private int sickLeavesCount;
}
