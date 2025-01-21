package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.repository.AppointmentRepository;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
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
class DiagnosisRepositoryTest {

    @Autowired
    private DiagnosisRepository diagnosisRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Appointment sampleAppointment;

    @BeforeEach
    void setUp() {
        // Create and save a sample doctor
        Doctor sampleDoctor = new Doctor();
        sampleDoctor.setName("Dr. Jane Smith");
        sampleDoctor.setSpecialties("Cardiologist");
        sampleDoctor.setKeycloakUserId("keycloak-id-123"); // Set non-null KEYCLOAK_USER_ID
        sampleDoctor = doctorRepository.save(sampleDoctor);

        // Create and save a sample patient
        Patient samplePatient = new Patient();
        samplePatient.setName("John Doe");
        samplePatient.setPrimaryDoctor(sampleDoctor); // Set the primary doctor
        samplePatient = patientRepository.save(samplePatient);

        // Create and save a sample appointment with the doctor and patient
        sampleAppointment = new Appointment();
        sampleAppointment.setAppointmentDateTime(LocalDateTime.now());
        sampleAppointment.setPatient(samplePatient); // Set the patient
        sampleAppointment.setDoctor(sampleDoctor);   // Set the doctor
        sampleAppointment = appointmentRepository.save(sampleAppointment);

        // Create sample diagnoses associated with the appointment
        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setStatement("Hypertension");
        diagnosis1.setAppointment(sampleAppointment);

        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setStatement("Diabetes");
        diagnosis2.setAppointment(sampleAppointment);

        Diagnosis diagnosis3 = new Diagnosis();
        diagnosis3.setStatement("Hypertension"); // Duplicate statement
        diagnosis3.setAppointment(sampleAppointment);

        diagnosisRepository.save(diagnosis1);
        diagnosisRepository.save(diagnosis2);
        diagnosisRepository.save(diagnosis3);
    }

    @Test
    void findDistinctStatements_ShouldReturnDistinctStatements() {
        // Act
        List<String> statements = diagnosisRepository.findDistinctStatements();

        // Assert
        assertEquals(2, statements.size());
        assertTrue(statements.contains("Hypertension"));
        assertTrue(statements.contains("Diabetes"));
    }

    @Test
    void findByStatementIgnoreCase_ShouldReturnDiagnosesMatchingStatement() {
        // Act
        List<Diagnosis> diagnoses = diagnosisRepository.findByStatementIgnoreCase("HYPERTENSION");

        // Assert
        assertEquals(2, diagnoses.size());
        assertTrue(diagnoses.stream().allMatch(d -> d.getStatement().equalsIgnoreCase("Hypertension")));
    }

    @Test
    void findByStatementIgnoreCase_ShouldReturnEmptyList_WhenNoMatch() {
        // Act
        List<Diagnosis> diagnoses = diagnosisRepository.findByStatementIgnoreCase("Nonexistent");

        // Assert
        assertTrue(diagnoses.isEmpty());
    }
}
