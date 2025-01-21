// src/main/java/bg/nbu/medicalrecords/dto/DoctorDto.java
package bg.nbu.medicalrecords.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorDto {

    @NotNull(message = "ID cannot be null")
    private Long id;

    @NotBlank(message = "Keycloak user ID cannot be blank")
    private String keycloakUserId;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 255, message = "Name cannot exceed 255 characters")
    private String name;

    @Size(max = 500, message = "Specialties cannot exceed 500 characters")
    private String specialties;

    private boolean primaryCare; // No validation needed as primitive boolean cannot be null
}
