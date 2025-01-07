// LocalSyncService.java
package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalSyncService {
    private final DoctorService doctorService;
    private final PatientService patientService;
    private final KeycloakUserService keycloakUserService;
    private final UserService userService;

    public LocalSyncService(DoctorService doctorService, PatientService patientService, KeycloakUserService keycloakUserService, UserService userService) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.keycloakUserService = keycloakUserService;
        this.userService = userService;
    }

    @Transactional
    public void handleRoleChange(String userId, String newRole) {
        KeycloakUserDto kcData = keycloakUserService.findUserById(userId);

        if (doctorService.existsByKeycloakId(userId)) {
            doctorService.deleteByKeycloakUserId(userId);
        }
        if (patientService.existsByKeycloakId(userId)) {
            patientService.deleteByKeycloakUserId(userId);
        }

        if (newRole.equals("doctor")) {
            String name = (kcData.getFirstName() == null && kcData.getLastName() == null) ? kcData.getUsername() : kcData.getFirstName() + " " + kcData.getLastName();
            doctorService.createDoctorFromKeycloak(userId, name, userId);
        } else if (newRole.equals("patient")) {
            String name = (kcData.getFirstName() == null && kcData.getLastName() == null) ? kcData.getUsername() : kcData.getFirstName() + " " + kcData.getLastName();
            patientService.createPatientFromKeycloak(userId, name);
        } else if (newRole.equals("admin")) {
            userService.assignRole(userId, "admin");
        }
    }
}