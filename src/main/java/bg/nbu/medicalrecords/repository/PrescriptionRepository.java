package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
}