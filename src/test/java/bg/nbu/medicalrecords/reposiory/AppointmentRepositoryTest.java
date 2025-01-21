package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.repository.AppointmentRepository;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class AppointmentRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        // Create a sample doctor
        doctor = new Doctor();
        doctor.setKeycloakUserId("doctor-kc-id");
        doctor.setName("Dr. John");
        doctorRepository.save(doctor);

        // Create a sample patient
        patient = new Patient();
        patient.setKeycloakUserId("patient-kc-id");
        patient.setName("Patient Jane");
        patientRepository.save(patient);

        // Create sample appointments
        Appointment appointment1 = new Appointment();
        appointment1.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment1.setDoctor(doctor);
        appointment1.setPatient(patient);

        Appointment appointment2 = new Appointment();
        appointment2.setAppointmentDateTime(LocalDateTime.now().plusDays(2));
        appointment2.setDoctor(doctor);
        appointment2.setPatient(patient);

        appointmentRepository.save(appointment1);
        appointmentRepository.save(appointment2);
    }

    @Test
    void findByPatient_KeycloakUserId_ShouldReturnAppointmentsForPatient() {
        // Act
        List<Appointment> appointments = appointmentRepository.findByPatient_KeycloakUserId("patient-kc-id");

        // Assert
        assertEquals(2, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getPatient().getKeycloakUserId().equals("patient-kc-id")));
    }

    @Test
    void findByDoctor_KeycloakUserId_ShouldReturnAppointmentsForDoctor() {
        // Act
        List<Appointment> appointments = appointmentRepository.findByDoctor_KeycloakUserId("doctor-kc-id");

        // Assert
        assertEquals(2, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getDoctor().getKeycloakUserId().equals("doctor-kc-id")));
    }

    @Test
    void findByPatient_Id_ShouldReturnAppointmentsForPatient() {
        // Act
        List<Appointment> appointments = appointmentRepository.findByPatient_Id(patient.getId());

        // Assert
        assertEquals(2, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getPatient().getId().equals(patient.getId())));
    }

    @Test
    void findByDoctor_Id_ShouldReturnAppointmentsForDoctor() {
        // Act
        List<Appointment> appointments = appointmentRepository.findByDoctor_Id(doctor.getId());

        // Assert
        assertEquals(2, appointments.size());
        assertTrue(appointments.stream().allMatch(a -> a.getDoctor().getId().equals(doctor.getId())));
    }
}
