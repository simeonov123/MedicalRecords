package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    boolean existsByKeycloakUserId(String keycloakUserId);
    Patient findByKeycloakUserId(String keycloakUserId);
    void deleteByKeycloakUserId(String keycloakUserId);


}
