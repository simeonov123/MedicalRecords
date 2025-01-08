package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Medication;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicationRepository extends JpaRepository<Medication, Long> {
}