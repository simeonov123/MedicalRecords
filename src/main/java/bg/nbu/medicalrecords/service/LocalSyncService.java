// LocalSyncService.java

package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * LocalSyncService is responsible for updating local (DB) records
 * in response to a user’s role change in Keycloak. For example, if
 * a user is promoted to 'doctor', we create a Doctor entity in the DB;
 * if changed to 'patient', we create a Patient entity, etc.
 */
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

    /**
     * Handle a role change for a user in Keycloak by syncing it
     * to local database tables (doctor, patient, etc.).
     */
    @Transactional
    public void handleRoleChange(String userId, String newRole) {
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
            // Use either firstName + lastName or fallback to username
            String name =
                    (kcData.getFirstName() == null && kcData.getLastName() == null)
                            ? kcData.getUsername()
                            : kcData.getFirstName() + " " + kcData.getLastName();

            // createDoctorFromKeycloak(...) typically inserts into the 'doctors' table
            doctorService.createDoctorFromKeycloak(userId, name, userId);

        } else if ("patient".equals(newRole)) {
            String name =
                    (kcData.getFirstName() == null && kcData.getLastName() == null)
                            ? kcData.getUsername()
                            : kcData.getFirstName() + " " + kcData.getLastName();

            // createPatientFromKeycloak(...) typically inserts into the 'patients' table
            patientService.createPatientFromKeycloak(userId, name);

        } else if ("admin".equals(newRole)) {
            // Optionally, if you manage 'admin' roles locally
            // you can call a method to update the local user record or
            // set the role to 'admin' in the DB. For example:
            userService.assignRole(userId, "admin");
        }

        // If newRole == "user" or anything else, you could also handle that here.
    }
}
