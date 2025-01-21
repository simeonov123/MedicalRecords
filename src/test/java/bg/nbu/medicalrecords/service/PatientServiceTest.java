package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
import bg.nbu.medicalrecords.exception.ResourceNotFoundException;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;

import java.util.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PatientServiceTest {

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private PatientService patientService;

    @BeforeEach
    void setUp() {
        // Initialize common objects if needed
    }

    /**
     * Test successfully creating a patient from Keycloak.
     */
    @Test
    void createPatientFromKeycloak_Success() {
        // Arrange
        String kcUserId = "kc-123";
        String name = "John Doe";

        User user = new User();
        user.setKeycloakUserId(kcUserId);
        user.setRole("patient");

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(user);
        // No need to call createUser if role is already "patient"

        Patient patient = new Patient();
        patient.setKeycloakUserId(kcUserId);
        patient.setName(name);
        patient.setHealthInsurancePaid(false);

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // Act
        Patient result = patientService.createPatientFromKeycloak(kcUserId, name);

        // Assert
        assertNotNull(result);
        assertEquals(kcUserId, result.getKeycloakUserId());
        assertEquals(name, result.getName());
        assertFalse(result.isHealthInsurancePaid());

        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, never()).createUser(any(User.class));
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    /**
     * Test failure when creating a patient from Keycloak due to user not found.
     */
    @Test
    void createPatientFromKeycloak_UserNotFound() {
        // Arrange
        String kcUserId = "kc-123";
        String name = "John Doe";

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.createPatientFromKeycloak(kcUserId, name);
        });

        assertEquals("User not found with keycloak id: " + kcUserId, exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, never()).createUser(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /**
     * Test failure when creating a patient from Keycloak due to Keycloak ID mismatch.
     */
    @Test
    void createPatientFromKeycloak_KeycloakIdMismatch() {
        // Arrange
        String kcUserId = "kc-123";
        String name = "John Doe";

        User user = new User();
        user.setKeycloakUserId("kc-456");
        user.setRole("patient");

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(user);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.createPatientFromKeycloak(kcUserId, name);
        });

        assertEquals("User keycloak id mismatch", exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, never()).createUser(any(User.class));
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /**
     * Test successfully creating a patient from Keycloak when user role is not patient.
     */
    @Test
    void createPatientFromKeycloak_UserRoleNotPatient() {
        // Arrange
        String kcUserId = "kc-123";
        String name = "John Doe";

        User user = new User();
        user.setKeycloakUserId(kcUserId);
        user.setRole("doctor");

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(user);
        when(userService.createUser(user)).thenReturn(user);

        Patient patient = new Patient();
        patient.setKeycloakUserId(kcUserId);
        patient.setName(name);
        patient.setHealthInsurancePaid(false);

        when(patientRepository.save(any(Patient.class))).thenReturn(patient);

        // Act
        Patient result = patientService.createPatientFromKeycloak(kcUserId, name);

        // Assert
        assertNotNull(result);
        assertEquals(kcUserId, result.getKeycloakUserId());
        assertEquals(name, result.getName());
        assertFalse(result.isHealthInsurancePaid());

        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, times(1)).createUser(user);
        verify(patientRepository, times(1)).save(any(Patient.class));
    }

    /**
     * Test successfully checking existence by Keycloak ID.
     */
    @Test
    void existsByKeycloakId_True() {
        // Arrange
        String userId = "kc-123";
        when(patientRepository.existsByKeycloakUserId(userId)).thenReturn(true);

        // Act
        boolean exists = patientService.existsByKeycloakId(userId);

        // Assert
        assertTrue(exists);
        verify(patientRepository, times(1)).existsByKeycloakUserId(userId);
    }

    /**
     * Test successfully checking non-existence by Keycloak ID.
     */
    @Test
    void existsByKeycloakId_False() {
        // Arrange
        String userId = "kc-123";
        when(patientRepository.existsByKeycloakUserId(userId)).thenReturn(false);

        // Act
        boolean exists = patientService.existsByKeycloakId(userId);

        // Assert
        assertFalse(exists);
        verify(patientRepository, times(1)).existsByKeycloakUserId(userId);
    }

    /**
     * Test successfully deleting a patient by Keycloak ID.
     */
    @Test
    void deleteByKeycloakUserId_Success() {
        // Arrange
        String userId = "kc-123";
        doNothing().when(patientRepository).deleteByKeycloakUserId(userId);

        // Act
        patientService.deleteByKeycloakUserId(userId);

        // Assert
        verify(patientRepository, times(1)).deleteByKeycloakUserId(userId);
    }

    /**
     * Test failure when deleting a patient by Keycloak ID.
     */
    @Test
    void deleteByKeycloakUserId_UserNotFound() {
        // Arrange
        String userId = "kc-123";
        doThrow(new ResourceNotFoundException("User not found")).when(patientRepository).deleteByKeycloakUserId(userId);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deleteByKeycloakUserId(userId);
        });

        assertEquals("User not found", exception.getMessage());
        verify(patientRepository, times(1)).deleteByKeycloakUserId(userId);
    }


    /**
     * Test failure when creating a patient due to doctor not found.
     */
    @Test
    void createPatient_DoctorNotFound() {
        // Arrange
        CreatePatientDto dto = new CreatePatientDto();
        dto.setName("John Doe");
        dto.setHealthInsurancePaid(true);
        dto.setPrimaryDoctorId(1L);

        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.createPatient(dto);
        });

        assertEquals("Doctor not found with id: 1", exception.getMessage());
        verify(doctorRepository, times(1)).findById(1L);
        verify(patientRepository, never()).save(any(Patient.class));
    }



    /**
     * Test failure when updating a patient due to patient not found.
     */
    @Test
    void updatePatient_PatientNotFound() {
        // Arrange
        String keycloakUserId = "kc-123";
        UpdatePatientDto dto = new UpdatePatientDto();
        dto.setHealthInsurancePaid(false);
        dto.setPrimaryDoctorId(2L);

        when(patientRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(keycloakUserId, dto);
        });

        assertEquals("Patient not found with keycloakUserId: " + keycloakUserId, exception.getMessage());
        verify(patientRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(userService, never()).findByKeycloakUserId(anyString());
        verify(doctorRepository, never()).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /**
     * Test failure when updating a patient due to doctor not found.
     */
    @Test
    void updatePatient_DoctorNotFound() {
        // Arrange
        String keycloakUserId = "kc-123";
        UpdatePatientDto dto = new UpdatePatientDto();
        dto.setHealthInsurancePaid(false);
        dto.setPrimaryDoctorId(2L);

        Patient patient = new Patient();
        patient.setKeycloakUserId(keycloakUserId);
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setId(1L);

        User user = new User();
        user.setKeycloakUserId(keycloakUserId);
        user.setFirstName("John");
        user.setLastName("Doe");

        when(patientRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(patient);
        when(userService.findByKeycloakUserId(keycloakUserId)).thenReturn(user);
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updatePatient(keycloakUserId, dto);
        });

        assertEquals("Doctor not found with id: 2", exception.getMessage());
        verify(patientRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(userService, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, times(1)).findById(2L);
        verify(patientRepository, never()).save(any(Patient.class));
    }

    /**
     * Test successfully deleting a patient by ID.
     */
    @Test
    void deletePatient_Success() {
        // Arrange
        Long patientId = 1L;
        when(patientRepository.existsById(patientId)).thenReturn(true);
        doNothing().when(patientRepository).deleteById(patientId);

        // Act
        patientService.deletePatient(patientId);

        // Assert
        verify(patientRepository, times(1)).existsById(patientId);
        verify(patientRepository, times(1)).deleteById(patientId);
    }

    /**
     * Test failure when deleting a patient by ID due to patient not found.
     */
    @Test
    void deletePatient_PatientNotFound() {
        // Arrange
        Long patientId = 1L;
        when(patientRepository.existsById(patientId)).thenReturn(false);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.deletePatient(patientId);
        });

        assertEquals("Patient not found with id: " + patientId, exception.getMessage());
        verify(patientRepository, times(1)).existsById(patientId);
        verify(patientRepository, never()).deleteById(patientId);
    }



    /**
     * Test successfully finding all patients when repository is empty.
     */
    @Test
    void findAll_Empty() {
        // Arrange
        when(patientRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<PatientDto> result = patientService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(patientRepository, times(1)).findAll();
        verifyNoMoreInteractions(userService);
    }


    /**
     * Test failure when finding a patient by ID.
     */
    @Test
    void findById_PatientNotFound() {
        // Arrange
        Long patientId = 1L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.findById(patientId);
        });

        assertEquals("Patient not found with id: " + patientId, exception.getMessage());
        verify(patientRepository, times(1)).findById(patientId);
    }



    /**
     * Test failure when finding a patient by EGN due to user not being a patient.
     */
    @Test
    void findByEgn_UserNotPatient() {
        // Arrange
        String egn = "1234567890";
        User user = new User();
        user.setKeycloakUserId("kc-123");
        user.setEgn(egn);
        user.setRole("doctor");

        Patient patient = new Patient();
        patient.setKeycloakUserId("kc-123");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);

        when(userService.findByEgn(egn)).thenReturn(user);
        when(patientRepository.findByKeycloakUserId("kc-123")).thenReturn(patient);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.findByEgn(egn);
        });

        assertEquals("User not a patient", exception.getMessage());
        verify(userService, times(1)).findByEgn(egn);
        verify(patientRepository, times(1)).findByKeycloakUserId("kc-123");
        verify(userService, never()).findByKeycloakUserId(anyString());
    }

    /**
     * Test failure when finding a patient by EGN due to patient not found.
     */
    @Test
    void findByEgn_PatientNotFound() {
        // Arrange
        String egn = "1234567890";
        User user = new User();
        user.setKeycloakUserId("kc-123");
        user.setEgn(egn);
        user.setRole("patient");

        when(userService.findByEgn(egn)).thenReturn(user);
        when(patientRepository.findByKeycloakUserId("kc-123")).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.findByEgn(egn);
        });

        assertEquals("Patient not found with EGN: " + egn, exception.getMessage());
        verify(userService, times(1)).findByEgn(egn);
        verify(patientRepository, times(1)).findByKeycloakUserId("kc-123");
    }

    /**
     * Test successfully assigning a primary doctor to a patient.
     */
    @Test
    void assignPrimaryDoctor_Success() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setKeycloakUserId("kc-123");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);

        User user = new User();
        user.setKeycloakUserId("kc-123");
        user.setRole("patient");

        Doctor doctor = new Doctor();
        doctor.setId(2L);
        doctor.setName("Dr. Adams");
        doctor.setPrimaryCare(false);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(userService.findByKeycloakUserId("kc-123")).thenReturn(user);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(doctor));
        when(patientRepository.save(patient)).thenReturn(patient);
        when(doctorRepository.save(doctor)).thenReturn(doctor);

        // Act
        patientService.assignPrimaryDoctor(patientId, doctorId);

        // Assert
        assertEquals(doctor, patient.getPrimaryDoctor());
        assertTrue(doctor.isPrimaryCare());

        verify(patientRepository, times(1)).findById(patientId);
        verify(userService, times(1)).findByKeycloakUserId("kc-123");
        verify(doctorRepository, times(1)).findById(doctorId);
        verify(patientRepository, times(1)).save(patient);
        verify(doctorRepository, times(1)).save(doctor);
    }

    /**
     * Test failure when assigning a primary doctor due to patient not found.
     */
    @Test
    void assignPrimaryDoctor_PatientNotFound() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.assignPrimaryDoctor(patientId, doctorId);
        });

        assertEquals("Patient not found with id: " + patientId, exception.getMessage());
        verify(patientRepository, times(1)).findById(patientId);
        verify(userService, never()).findByKeycloakUserId(anyString());
        verify(doctorRepository, never()).findById(anyLong());
        verify(patientRepository, never()).save(any(Patient.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * Test failure when assigning a primary doctor due to doctor not found.
     */
    @Test
    void assignPrimaryDoctor_DoctorNotFound() {
        // Arrange
        Long patientId = 1L;
        Long doctorId = 2L;

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setKeycloakUserId("kc-123");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);

        User user = new User();
        user.setKeycloakUserId("kc-123");
        user.setRole("patient");

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(userService.findByKeycloakUserId("kc-123")).thenReturn(user);
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.assignPrimaryDoctor(patientId, doctorId);
        });

        assertEquals("Doctor not found with id: " + doctorId, exception.getMessage());
        verify(patientRepository, times(1)).findById(patientId);
        verify(userService, times(1)).findByKeycloakUserId("kc-123");
        verify(doctorRepository, times(1)).findById(doctorId);
        verify(patientRepository, never()).save(any(Patient.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }



    /**
     * Test successfully updating health insurance status.
     */
    @Test
    void updateHealthInsuranceStatus_Success() {
        // Arrange
        Long patientId = 1L;
        Boolean newStatus = false;

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setHealthInsurancePaid(true);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(patientRepository.save(patient)).thenReturn(patient);

        // Act
        patientService.updateHealthInsuranceStatus(patientId, newStatus);

        // Assert
        assertFalse(patient.isHealthInsurancePaid());
        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, times(1)).save(patient);
    }

    /**
     * Test failure when updating health insurance status due to patient not found.
     */
    @Test
    void updateHealthInsuranceStatus_PatientNotFound() {
        // Arrange
        Long patientId = 1L;
        Boolean newStatus = false;

        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.updateHealthInsuranceStatus(patientId, newStatus);
        });

        assertEquals("Patient not found with id: " + patientId, exception.getMessage());
        verify(patientRepository, times(1)).findById(patientId);
        verify(patientRepository, never()).save(any(Patient.class));
    }



    /**
     * Test failure when finding a patient by Keycloak User ID.
     */
    @Test
    void findByKeycloakUserId_PatientNotFound() {
        // Arrange
        String keycloakUserId = "kc-123";
        when(patientRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(null);

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.findByKeycloakUserId(keycloakUserId);
        });

        assertEquals("Patient not found with keycloakUserId: " + keycloakUserId, exception.getMessage());
        verify(patientRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(userService, never()).findByKeycloakUserId(anyString());
    }

    /**
     * Test successfully finding a patient by internal ID.
     */
    @Test
    void findPatientById_Success() {
        // Arrange
        Long patientId = 1L;
        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setKeycloakUserId("kc-123");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);

        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));

        // Act
        Patient result = patientService.findPatientById(patientId);

        // Assert
        assertNotNull(result);
        assertEquals(patientId, result.getId());
        assertEquals("kc-123", result.getKeycloakUserId());

        verify(patientRepository, times(1)).findById(patientId);
    }

    /**
     * Test failure when finding a patient by internal ID.
     */
    @Test
    void findPatientById_PatientNotFound() {
        // Arrange
        Long patientId = 1L;
        when(patientRepository.findById(patientId)).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            patientService.findPatientById(patientId);
        });

        assertEquals("Patient not found with id: " + patientId, exception.getMessage());
        verify(patientRepository, times(1)).findById(patientId);
    }

    /**
     * Test successfully counting patients.
     */
    @Test
    void count_Success() {
        // Arrange
        when(patientRepository.count()).thenReturn(5L);

        // Act
        long count = patientService.count();

        // Assert
        assertEquals(5L, count);
        verify(patientRepository, times(1)).count();
    }



    /**
     * Test failure when finding all patients by primary doctor ID.
     */
    @Test
    void findAllByPrimaryDoctorId_DoctorNotFound() {
        // Arrange
        Long doctorId = 1L;

        when(patientRepository.findByPrimaryDoctor_Id(doctorId)).thenReturn(Collections.emptyList());

        // Act
        List<PatientDto> result = patientService.findAllByPrimaryDoctorId(doctorId);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(patientRepository, times(1)).findByPrimaryDoctor_Id(doctorId);
        verifyNoMoreInteractions(userService);
    }
}
