package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    private Doctor sampleDoctor;

    @BeforeEach
    void setUp() {
        // Create and save a sample doctor
        sampleDoctor = new Doctor();
        sampleDoctor.setName("Dr. Jane Doe");
        sampleDoctor.setSpecialties("Cardiologist");
        sampleDoctor.setKeycloakUserId("unique-keycloak-id");
        sampleDoctor = doctorRepository.save(sampleDoctor);
    }

    @Test
    void findByKeycloakUserId_ShouldReturnDoctor() {
        // Act
        Doctor foundDoctor = doctorRepository.findByKeycloakUserId("unique-keycloak-id");

        // Assert
        assertNotNull(foundDoctor);
        assertEquals(sampleDoctor.getId(), foundDoctor.getId());
        assertEquals(sampleDoctor.getName(), foundDoctor.getName());
    }

    @Test
    void findByKeycloakUserId_ShouldReturnNull_WhenNotFound() {
        // Act
        Doctor foundDoctor = doctorRepository.findByKeycloakUserId("nonexistent-id");

        // Assert
        assertNull(foundDoctor);
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnTrue_WhenExists() {
        // Act
        boolean exists = doctorRepository.existsByKeycloakUserId("unique-keycloak-id");

        // Assert
        assertTrue(exists);
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnFalse_WhenNotExists() {
        // Act
        boolean exists = doctorRepository.existsByKeycloakUserId("nonexistent-id");

        // Assert
        assertFalse(exists);
    }

    @Test
    void deleteByKeycloakUserId_ShouldDeleteDoctor() {
        // Act
        doctorRepository.deleteByKeycloakUserId("unique-keycloak-id");
        Doctor foundDoctor = doctorRepository.findByKeycloakUserId("unique-keycloak-id");

        // Assert
        assertNull(foundDoctor);
    }
}
