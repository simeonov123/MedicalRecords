package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SickLeaveRepositoryTest {

    @Autowired
    private SickLeaveRepository sickLeaveRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Long savedSickLeaveId;
    private Appointment sampleAppointment;

    @BeforeEach
    void setUp() {
        // Create and save a sample Doctor
        Doctor doctor = new Doctor();
        doctor.setName("Dr. Smith");
        doctor.setSpecialties("General Medicine");
        doctor.setKeycloakUserId("doctor-keycloak-id");
        doctor.setPrimaryCare(true);
        doctor = doctorRepository.save(doctor);

        // Create and save a sample Patient
        Patient patient = new Patient();
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(doctor);
        patient.setKeycloakUserId("patient-keycloak-id");
        patient = patientRepository.save(patient);

        // Create and save a sample Appointment
        sampleAppointment = new Appointment();
        sampleAppointment.setAppointmentDateTime(LocalDateTime.now());
        sampleAppointment.setCreatedAt(LocalDateTime.now());
        sampleAppointment.setUpdatedAt(LocalDateTime.now());
        sampleAppointment.setDoctor(doctor);
        sampleAppointment.setPatient(patient);
        sampleAppointment = appointmentRepository.save(sampleAppointment);

        // Create and save a sample Sick Leave
        SickLeave sickLeave = new SickLeave();
        sickLeave.setAppointment(sampleAppointment);
        sickLeave.setStartDate(LocalDate.now());
        sickLeave.setEndDate(LocalDate.now().plusDays(7));
        sickLeave.setReason("Flu");
        sickLeave.setCreatedAt(LocalDateTime.now());
        sickLeave.setUpdatedAt(LocalDateTime.now());
        SickLeave savedSickLeave = sickLeaveRepository.save(sickLeave);
        savedSickLeaveId = savedSickLeave.getId();
    }

    @Test
    void save_ShouldPersistSickLeave() {
        SickLeave sickLeave = new SickLeave();
        sickLeave.setAppointment(sampleAppointment);
        sickLeave.setStartDate(LocalDate.now());
        sickLeave.setEndDate(LocalDate.now().plusDays(14));
        sickLeave.setReason("Extended Recovery");
        sickLeave.setCreatedAt(LocalDateTime.now());
        sickLeave.setUpdatedAt(LocalDateTime.now());

        SickLeave savedSickLeave = sickLeaveRepository.save(sickLeave);

        assertNotNull(savedSickLeave.getId());
        assertEquals("Extended Recovery", savedSickLeave.getReason());
    }

    @Test
    void findById_ShouldReturnSickLeave() {
        Optional<SickLeave> optionalSickLeave = sickLeaveRepository.findById(savedSickLeaveId);

        assertTrue(optionalSickLeave.isPresent());
        SickLeave sickLeave = optionalSickLeave.get();
        assertEquals("Flu", sickLeave.getReason());
        assertEquals(sampleAppointment.getId(), sickLeave.getAppointment().getId());
    }

    @Test
    void deleteById_ShouldRemoveSickLeave() {
        assertTrue(sickLeaveRepository.existsById(savedSickLeaveId));

        sickLeaveRepository.deleteById(savedSickLeaveId);

        assertFalse(sickLeaveRepository.existsById(savedSickLeaveId));
    }
}
