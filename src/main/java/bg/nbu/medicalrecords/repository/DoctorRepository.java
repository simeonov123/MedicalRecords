package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
}
