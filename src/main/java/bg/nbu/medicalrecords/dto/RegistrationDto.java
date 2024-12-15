package bg.nbu.medicalrecords.dto;

import lombok.Data;

@Data
public class RegistrationDto {
    private String username;
    private String email;
    private String password;
    private String desiredRole; // "patient" or "doctor"

    // getters and setters
}
