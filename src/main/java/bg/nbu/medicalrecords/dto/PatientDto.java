// src/main/java/bg/nbu/medicalrecords/dto/PatientDto.java
package bg.nbu.medicalrecords.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientDto {
    private Long id;
    private String name;
    private String egn;
    private boolean healthInsurancePaid;
    private Long primaryDoctorId;
    private String keycloakUserId;
}