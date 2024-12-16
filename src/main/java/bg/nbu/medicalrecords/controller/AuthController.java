package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.RegistrationDto;
import bg.nbu.medicalrecords.service.KeycloakService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final KeycloakService keycloakService;

    public AuthController(KeycloakService keycloakService) {
        this.keycloakService = keycloakService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody RegistrationDto registrationDto) {
        // 1. Create user in Keycloak
        String userId = keycloakService.createUser(
                registrationDto.getUsername(),
                registrationDto.getEmail(),
                registrationDto.getPassword(),
                registrationDto.getFirstName(),
                registrationDto.getLastName()
        );

        // 2. Assign default role
        if ("patient".equalsIgnoreCase(registrationDto.getDesiredRole())) {
            keycloakService.assignRole(userId, "patient");
        } else if ("doctor".equalsIgnoreCase(registrationDto.getDesiredRole())) {
            keycloakService.assignRole(userId, "user");  // placeholder role
        } else {
            keycloakService.assignRole(userId, "user");
        }

        return ResponseEntity.ok("User registered successfully");
    }

}
