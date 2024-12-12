package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByDoctorId(Long doctorId);
    List<Appointment> findByPatientId(Long patientId);
    List<Appointment> findByDateBetween(LocalDate start, LocalDate end);
    List<Appointment> findByDoctorIdAndDateBetween(Long doctorId, LocalDate start, LocalDate end);
}
