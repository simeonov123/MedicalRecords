package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Patient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient, Long> {
    Patient findByEgn(String egn);
}
