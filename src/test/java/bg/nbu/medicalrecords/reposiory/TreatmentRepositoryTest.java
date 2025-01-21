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
class TreatmentRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    private Diagnosis sampleDiagnosis;

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
        Appointment appointment = new Appointment();
        appointment.setAppointmentDateTime(LocalDateTime.now());
        appointment.setCreatedAt(LocalDateTime.now());
        appointment.setUpdatedAt(LocalDateTime.now());
        appointment.setDoctor(doctor);
        appointment.setPatient(patient);
        appointment = appointmentRepository.save(appointment);

        // Create and save a sample Diagnosis
        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setStatement("Sample Diagnosis");
        diagnosis.setDiagnosedDate(LocalDateTime.now());
        diagnosis.setCreatedAt(LocalDateTime.now());
        diagnosis.setUpdatedAt(LocalDateTime.now());
        diagnosis.setAppointment(appointment);
        sampleDiagnosis = diagnosisRepository.save(diagnosis);
    }

    @Test
    void save_ShouldPersistTreatment() {
        Treatment treatment = new Treatment();
        treatment.setDescription("Physical Therapy");
        treatment.setStartDate(LocalDate.now());
        treatment.setEndDate(LocalDate.now().plusDays(7));
        treatment.setCreatedAt(LocalDateTime.now());
        treatment.setUpdatedAt(LocalDateTime.now());
        treatment.setDiagnosis(sampleDiagnosis);

        Treatment savedTreatment = treatmentRepository.save(treatment);

        assertNotNull(savedTreatment.getId());
        assertEquals("Physical Therapy", savedTreatment.getDescription());
    }

    @Test
    void findById_ShouldReturnTreatment() {
        Treatment treatment = new Treatment();
        treatment.setDescription("Physical Therapy");
        treatment.setStartDate(LocalDate.now());
        treatment.setEndDate(LocalDate.now().plusDays(7));
        treatment.setCreatedAt(LocalDateTime.now());
        treatment.setUpdatedAt(LocalDateTime.now());
        treatment.setDiagnosis(sampleDiagnosis);
        Treatment savedTreatment = treatmentRepository.save(treatment);

        Optional<Treatment> optionalTreatment = treatmentRepository.findById(savedTreatment.getId());

        assertTrue(optionalTreatment.isPresent());
        Treatment fetchedTreatment = optionalTreatment.get();
        assertEquals("Physical Therapy", fetchedTreatment.getDescription());
        assertEquals(sampleDiagnosis.getId(), fetchedTreatment.getDiagnosis().getId());
    }

    @Test
    void deleteById_ShouldRemoveTreatment() {
        Treatment treatment = new Treatment();
        treatment.setDescription("Physical Therapy");
        treatment.setStartDate(LocalDate.now());
        treatment.setEndDate(LocalDate.now().plusDays(7));
        treatment.setCreatedAt(LocalDateTime.now());
        treatment.setUpdatedAt(LocalDateTime.now());
        treatment.setDiagnosis(sampleDiagnosis);
        Treatment savedTreatment = treatmentRepository.save(treatment);

        assertTrue(treatmentRepository.existsById(savedTreatment.getId()));

        treatmentRepository.deleteById(savedTreatment.getId());

        assertFalse(treatmentRepository.existsById(savedTreatment.getId()));
    }
}
