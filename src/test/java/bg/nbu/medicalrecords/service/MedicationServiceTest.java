package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Medication;
import bg.nbu.medicalrecords.exception.MedicationNotFoundException;
import bg.nbu.medicalrecords.repository.MedicationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicationServiceTest {

    @Mock
    private MedicationRepository medicationRepository;

    @InjectMocks
    private MedicationService medicationService;

    @Test
    void getAll_ShouldReturnListOfMedications() {
        // Arrange
        Medication medication1 = new Medication();
        medication1.setId(1L);
        medication1.setMedicationName("Medication A");

        Medication medication2 = new Medication();
        medication2.setId(2L);
        medication2.setMedicationName("Medication B");

        when(medicationRepository.findAll()).thenReturn(Arrays.asList(medication1, medication2));

        // Act
        List<Medication> result = medicationService.getAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Medication A", result.get(0).getMedicationName());
        assertEquals("Medication B", result.get(1).getMedicationName());
        verify(medicationRepository, times(1)).findAll();
    }

    @Test
    void findById_ShouldReturnMedication_WhenFound() {
        // Arrange
        Long medicationId = 1L;
        Medication medication = new Medication();
        medication.setId(medicationId);
        medication.setMedicationName("Medication A");

        when(medicationRepository.findById(medicationId)).thenReturn(Optional.of(medication));

        // Act
        Medication result = medicationService.findById(medicationId);

        // Assert
        assertNotNull(result);
        assertEquals(medicationId, result.getId());
        assertEquals("Medication A", result.getMedicationName());
        verify(medicationRepository, times(1)).findById(medicationId);
    }

    @Test
    void findById_ShouldThrowException_WhenNotFound() {
        // Arrange
        Long medicationId = 1L;
        when(medicationRepository.findById(medicationId)).thenReturn(Optional.empty());

        // Act & Assert
        MedicationNotFoundException exception = assertThrows(MedicationNotFoundException.class,
                () -> medicationService.findById(medicationId));
        assertEquals("Medication not found with id: " + medicationId, exception.getMessage());
        verify(medicationRepository, times(1)).findById(medicationId);
    }
}