package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.SickLeave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SickLeaveRepository extends JpaRepository<SickLeave, Long> {
}
