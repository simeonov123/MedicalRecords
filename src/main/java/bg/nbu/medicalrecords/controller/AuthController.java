package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.RegistrationDto;
import bg.nbu.medicalrecords.service.KeycloakService;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final KeycloakService keycloakService;
    private final UserService userService;

    private final PatientService patientService;
    public AuthController(KeycloakService keycloakService, UserService userService, PatientService patientService) {
        this.keycloakService = keycloakService;
        this.userService = userService;
        this.patientService = patientService;
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

        // 2. Create user in the database
        User user = new User();
        user.setKeycloakUserId(userId);
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setFirstName(registrationDto.getFirstName());
        user.setLastName(registrationDto.getLastName());
        user.setEgn(registrationDto.getEgn());
        User savedUser = userService.createUser(user);

        // 3. Assign default role
        if ("patient".equalsIgnoreCase(registrationDto.getDesiredRole())) {
            keycloakService.assignRole(userId, "patient");
            savedUser.setRole("patient");
            userService.createUser(savedUser);
            patientService.createPatientFromKeycloak(userId, registrationDto.getUsername());
        } else if ("doctor".equalsIgnoreCase(registrationDto.getDesiredRole())) {
            keycloakService.assignRole(userId, "user");
            savedUser.setRole("user");
            userService.createUser(savedUser);

        } else {
            keycloakService.assignRole(userId, "user");
            savedUser.setRole("user");
            userService.createUser(savedUser);
        }

        return ResponseEntity.ok("User registered successfully");
    }

}
