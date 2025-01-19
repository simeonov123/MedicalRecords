package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class KeycloakPlusLocalUserDto {
    private String id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String role;
    private String egn;
    private boolean emailVerified;
}
