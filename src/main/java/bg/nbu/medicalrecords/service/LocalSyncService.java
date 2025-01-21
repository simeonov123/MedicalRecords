package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import bg.nbu.medicalrecords.exception.LocalSyncException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LocalSyncService {

    private final DoctorService doctorService;
    private final PatientService patientService;
    private final KeycloakService keycloakService;
    private final UserService userService;

    public LocalSyncService(
            DoctorService doctorService,
            PatientService patientService,
            KeycloakService keycloakService, UserService userService
    ) {
        this.doctorService = doctorService;
        this.patientService = patientService;
        this.keycloakService = keycloakService;
        this.userService = userService;
    }

    @Transactional
    public void handleRoleChange(String userId, String newRole) {
        try {
            // 1) Fetch user details from Keycloak
            KeycloakUserDto kcData = keycloakService.findUserById(userId);

            // 2) If a Doctor record exists for this user, remove it
            if (doctorService.existsByKeycloakId(userId)) {
                doctorService.deleteByKeycloakUserId(userId);
            }
            // 3) If a Patient record exists for this user, remove it
            if (patientService.existsByKeycloakId(userId)) {
                patientService.deleteByKeycloakUserId(userId);
            }

            // 4) Recreate local records depending on the new role
            if ("doctor".equals(newRole)) {
                String name = (kcData.getFirstName() == null && kcData.getLastName() == null)
                        ? kcData.getUsername()
                        : kcData.getFirstName() + " " + kcData.getLastName();
                doctorService.createDoctorFromKeycloak(userId, name, userId);

            } else if ("patient".equals(newRole)) {
                String name = (kcData.getFirstName() == null && kcData.getLastName() == null)
                        ? kcData.getUsername()
                        : kcData.getFirstName() + " " + kcData.getLastName();
                patientService.createPatientFromKeycloak(userId, name);

            } else if ("admin".equals(newRole)) {
                userService.assignRole(userId, "admin");
            }

        } catch (Exception e) {
            throw new LocalSyncException("Failed to handle role change for user ID: " + userId, e);
        }
    }
}