package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientRepositoryTest {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Doctor primaryDoctor;

    @BeforeEach
    void setUp() {
        // Create and save a primary doctor
        primaryDoctor = new Doctor();
        primaryDoctor.setKeycloakUserId("doctor-keycloak-id");
        primaryDoctor.setName("Dr. John Smith");
        doctorRepository.save(primaryDoctor);

        // Create and save patients
        Patient patient1 = new Patient();
        patient1.setName("Patient One");
        patient1.setKeycloakUserId("patient1-keycloak-id");
        patient1.setPrimaryDoctor(primaryDoctor);

        Patient patient2 = new Patient();
        patient2.setName("Patient Two");
        patient2.setKeycloakUserId("patient2-keycloak-id");
        patient2.setPrimaryDoctor(primaryDoctor);

        patientRepository.save(patient1);
        patientRepository.save(patient2);
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnTrueForExistingUserId() {
        // Act
        boolean exists = patientRepository.existsByKeycloakUserId("patient1-keycloak-id");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnFalseForNonExistingUserId() {
        // Act
        boolean exists = patientRepository.existsByKeycloakUserId("nonexistent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    void findByKeycloakUserId_ShouldReturnPatientForValidUserId() {
        // Act
        Patient patient = patientRepository.findByKeycloakUserId("patient1-keycloak-id");

        // Assert
        assertNotNull(patient);
        assertEquals("Patient One", patient.getName());
    }

    @Test
    void findByKeycloakUserId_ShouldReturnNullForInvalidUserId() {
        // Act
        Patient patient = patientRepository.findByKeycloakUserId("nonexistent-id");

        // Assert
        assertNull(patient);
    }

    @Test
    void deleteByKeycloakUserId_ShouldRemovePatient() {
        // Act
        patientRepository.deleteByKeycloakUserId("patient1-keycloak-id");

        // Assert
        assertFalse(patientRepository.existsByKeycloakUserId("patient1-keycloak-id"));
    }

    @Test
    void findByPrimaryDoctor_Id_ShouldReturnPatientsForDoctor() {
        // Act
        List<Patient> patients = patientRepository.findByPrimaryDoctor_Id(primaryDoctor.getId());

        // Assert
        assertEquals(2, patients.size());
        assertTrue(patients.stream().anyMatch(p -> p.getName().equals("Patient One")));
        assertTrue(patients.stream().anyMatch(p -> p.getName().equals("Patient Two")));
    }

    @Test
    void findByPrimaryDoctor_Id_ShouldReturnEmptyListForDoctorWithoutPatients() {
        // Act
        List<Patient> patients = patientRepository.findByPrimaryDoctor_Id(999L); // Nonexistent doctor ID

        // Assert
        assertTrue(patients.isEmpty());
    }
}
