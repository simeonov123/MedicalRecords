package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Treatment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TreatmentRepository extends JpaRepository<Treatment, Long> {
}