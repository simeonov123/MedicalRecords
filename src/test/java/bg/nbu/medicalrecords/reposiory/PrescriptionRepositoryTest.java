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
class PrescriptionRepositoryTest {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private TreatmentRepository treatmentRepository;

    @Autowired
    private MedicationRepository medicationRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    private Long savedPrescriptionId; // Store ID of the saved Prescription for later tests
    private Treatment sampleTreatment;
    private Medication sampleMedication;

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
        diagnosis = diagnosisRepository.save(diagnosis);

        // Create and save a sample Treatment
        sampleTreatment = new Treatment();
        sampleTreatment.setDescription("Physical therapy");
        sampleTreatment.setStartDate(LocalDate.now());
        sampleTreatment.setEndDate(LocalDate.now().plusDays(7));
        sampleTreatment.setCreatedAt(LocalDateTime.now());
        sampleTreatment.setUpdatedAt(LocalDateTime.now());
        sampleTreatment.setDiagnosis(diagnosis);
        sampleTreatment = treatmentRepository.save(sampleTreatment);

        // Create and save a sample Medication
        sampleMedication = new Medication();
        sampleMedication.setMedicationName("Paracetamol");
        sampleMedication.setStrength("500mg");
        sampleMedication.setCreatedAt(LocalDateTime.now());
        sampleMedication.setUpdatedAt(LocalDateTime.now());
        sampleMedication = medicationRepository.save(sampleMedication);

        // Create and save a sample Prescription
        Prescription prescription = new Prescription();
        prescription.setTreatment(sampleTreatment);
        prescription.setMedication(sampleMedication);
        prescription.setDosage("1 tablet twice daily");
        prescription.setDuration(7);
        prescription.setCreatedAt(LocalDateTime.now());
        prescription.setUpdatedAt(LocalDateTime.now());
        Prescription savedPrescription = prescriptionRepository.save(prescription);
        savedPrescriptionId = savedPrescription.getId(); // Save the ID for later use
    }

    @Test
    void save_ShouldPersistPrescription() {
        Prescription prescription = new Prescription();
        prescription.setTreatment(sampleTreatment);
        prescription.setMedication(sampleMedication);
        prescription.setDosage("1000mg");
        prescription.setDuration(7);

        Prescription savedPrescription = prescriptionRepository.save(prescription);

        assertNotNull(savedPrescription.getId());
        assertEquals("1000mg", savedPrescription.getDosage());
    }

    @Test
    void findById_ShouldReturnPrescription() {
        Optional<Prescription> optionalPrescription = prescriptionRepository.findById(savedPrescriptionId);

        assertTrue(optionalPrescription.isPresent());
        Prescription prescription = optionalPrescription.get();
        assertEquals("1 tablet twice daily", prescription.getDosage());
        assertEquals(sampleTreatment.getId(), prescription.getTreatment().getId());
        assertEquals(sampleMedication.getId(), prescription.getMedication().getId());
    }

    @Test
    void deleteById_ShouldRemovePrescription() {
        assertTrue(prescriptionRepository.existsById(savedPrescriptionId));

        prescriptionRepository.deleteById(savedPrescriptionId);

        assertFalse(prescriptionRepository.existsById(savedPrescriptionId));
    }
}
