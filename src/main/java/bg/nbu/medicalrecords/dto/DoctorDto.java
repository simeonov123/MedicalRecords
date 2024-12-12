package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class DoctorDto {
    private Long id;
    private String uniqueIdentifier;
    private String name;
    private String specialties;
    private boolean primaryCare;

}
