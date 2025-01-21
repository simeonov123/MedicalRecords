package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.Medication;
import bg.nbu.medicalrecords.repository.MedicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MedicationRepositoryTest {

    @Autowired
    private MedicationRepository medicationRepository;

    private Medication sampleMedication;

    @BeforeEach
    void setUp() {
        // Create and save a sample medication
        sampleMedication = new Medication();
        sampleMedication.setMedicationName("Aspirin");
        sampleMedication.setStrength("Pain reliever");
        sampleMedication = medicationRepository.save(sampleMedication);
    }

    @Test
    void save_ShouldPersistMedication() {
        // Arrange
        Medication newMedication = new Medication();
        newMedication.setMedicationName("Paracetamol");
        newMedication.setStrength("Fever reducer");

        // Act
        Medication savedMedication = medicationRepository.save(newMedication);

        // Assert
        assertNotNull(savedMedication.getId());
        assertEquals("Paracetamol", savedMedication.getMedicationName());
        assertEquals("Fever reducer", savedMedication.getStrength());
    }

    @Test
    void findById_ShouldReturnMedication_WhenExists() {
        // Act
        Optional<Medication> foundMedication = medicationRepository.findById(sampleMedication.getId());

        // Assert
        assertTrue(foundMedication.isPresent());
        assertEquals(sampleMedication.getMedicationName(), foundMedication.get().getMedicationName());
    }

    @Test
    void findById_ShouldReturnEmpty_WhenNotExists() {
        // Act
        Optional<Medication> foundMedication = medicationRepository.findById(999L);

        // Assert
        assertFalse(foundMedication.isPresent());
    }

    @Test
    void findAll_ShouldReturnAllMedications() {
        // Act
        List<Medication> medications = medicationRepository.findAll();

        // Assert
        assertEquals(1, medications.size());
        assertEquals(sampleMedication.getMedicationName(), medications.get(0).getMedicationName());
    }

    @Test
    void deleteById_ShouldRemoveMedication() {
        // Act
        medicationRepository.deleteById(sampleMedication.getId());
        Optional<Medication> deletedMedication = medicationRepository.findById(sampleMedication.getId());

        // Assert
        assertFalse(deletedMedication.isPresent());
    }
}
