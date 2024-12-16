// src/main/java/bg/nbu/medicalrecords/dto/KeycloakUserDto.java
package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class KeycloakUserDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private boolean emailVerified;
}
