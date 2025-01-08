// src/main/java/bg/nbu/medicalrecords/dto/DoctorDto.java
package bg.nbu.medicalrecords.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {
    private Long id;
    private String keycloakUserId;
    private String name;
    private String specialties;
    private boolean primaryCare;
}