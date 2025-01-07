package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class PatientDto {
    private Long id;
    private String name;
    private String egn;
    private boolean healthInsurancePaid;
    private Long primaryDoctorId;
    private String keycloakUserId;

}
