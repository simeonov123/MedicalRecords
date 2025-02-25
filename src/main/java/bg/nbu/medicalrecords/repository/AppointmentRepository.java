package bg.nbu.medicalrecords.repository;

import bg.nbu.medicalrecords.domain.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    List<Appointment> findByPatient_KeycloakUserId(String keycloakUserId);

    List<Appointment> findByDoctor_KeycloakUserId(String keycloakUserId);

    List<Appointment> findByPatient_Id(Long id);

    List<Appointment> findByDoctor_Id(Long id);
}