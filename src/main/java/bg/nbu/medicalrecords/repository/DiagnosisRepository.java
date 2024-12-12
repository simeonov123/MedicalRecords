package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Diagnosis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiagnosisRepository extends JpaRepository<Diagnosis, Long> {
}
