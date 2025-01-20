package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DoctorServiceTest {

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationService authenticationService;

    @InjectMocks
    private DoctorService doctorService;

    private User existingUser;
    private User nonExistingUser;
    private Doctor existingDoctor;
    private Doctor anotherDoctor;
    private Doctor updatedDoctor;
    private Doctor newDoctor;

    @BeforeEach
    void setUp() {
        // Initialize common objects

        // Users
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setKeycloakUserId("user-123");
        existingUser.setRole("user");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");

        nonExistingUser = new User();
        nonExistingUser.setId(2L);
        nonExistingUser.setKeycloakUserId("user-456");
        nonExistingUser.setRole("user");
        nonExistingUser.setFirstName("Jane");
        nonExistingUser.setLastName("Smith");

        // Doctors
        existingDoctor = new Doctor();
        existingDoctor.setId(1L);
        existingDoctor.setName("Dr. Alice");
        existingDoctor.setKeycloakUserId("doctor-123");
        existingDoctor.setSpecialties("Cardiology");
        existingDoctor.setPrimaryCare(true);

        anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        anotherDoctor.setName("Dr. Bob");
        anotherDoctor.setKeycloakUserId("doctor-456");
        anotherDoctor.setSpecialties("Neurology");
        anotherDoctor.setPrimaryCare(false);

        updatedDoctor = new Doctor();
        updatedDoctor.setName("Dr. Alice Updated");
        updatedDoctor.setKeycloakUserId("doctor-123");
        updatedDoctor.setSpecialties("Pediatrics");
        updatedDoctor.setPrimaryCare(false);

        newDoctor = new Doctor();
        newDoctor.setId(3L);
        newDoctor.setName("Dr. Charlie");
        newDoctor.setKeycloakUserId("doctor-789");
        newDoctor.setSpecialties("Dermatology");
        newDoctor.setPrimaryCare(true);
    }

    /**
     * Test successfully creating a doctor from Keycloak.
     */
    @Test
    void createDoctorFromKeycloak_Success() {
        // Arrange
        String kcUserId = "user-123";
        String name = "Dr. Alice";
        String uniqueIdentifier = "doctor-123";

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(existingUser);
        when(doctorRepository.save(any(Doctor.class))).thenReturn(existingDoctor);
        when(userService.createUser(existingUser)).thenReturn(existingUser); // Changed from doNothing()

        // Act
        doctorService.createDoctorFromKeycloak(kcUserId, name, uniqueIdentifier);

        // Assert
        assertEquals("doctor", existingUser.getRole());
        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, times(1)).createUser(existingUser);
        ArgumentCaptor<Doctor> doctorCaptor = ArgumentCaptor.forClass(Doctor.class);
        verify(doctorRepository, times(1)).save(doctorCaptor.capture());

        Doctor savedDoctor = doctorCaptor.getValue();
        assertEquals(name, savedDoctor.getName());
        assertEquals(uniqueIdentifier, savedDoctor.getKeycloakUserId());
        assertFalse(savedDoctor.isPrimaryCare());
        assertEquals("N/A", savedDoctor.getSpecialties());
    }

    /**
     * Test creating a doctor from Keycloak when user is not found.
     */
    @Test
    void createDoctorFromKeycloak_Failure_UserNotFound() {
        // Arrange
        String kcUserId = "user-999";
        String name = "Dr. Unknown";
        String uniqueIdentifier = "doctor-999";

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.createDoctorFromKeycloak(kcUserId, name, uniqueIdentifier);
        });

        assertEquals("User not found with keycloak id: " + kcUserId, exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, never()).createUser(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * Test creating a doctor from Keycloak when user keycloak ID mismatch.
     */
    @Test
    void createDoctorFromKeycloak_Failure_KeycloakIdMismatch() {
        // Arrange
        String kcUserId = "user-123";
        String name = "Dr. Alice";
        String uniqueIdentifier = "doctor-999"; // Mismatch

        existingUser.setKeycloakUserId("user-456"); // Mismatch

        when(userService.findByKeycloakUserId(kcUserId)).thenReturn(existingUser);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.createDoctorFromKeycloak(kcUserId, name, uniqueIdentifier);
        });

        assertEquals("User keycloak id mismatch", exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(kcUserId);
        verify(userService, never()).createUser(any(User.class));
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * Test existsByKeycloakId returns true when doctor exists.
     */
    @Test
    void existsByKeycloakId_ReturnsTrue() {
        // Arrange
        String userId = "doctor-123";
        when(doctorRepository.existsByKeycloakUserId(userId)).thenReturn(true);

        // Act
        boolean exists = doctorService.existsByKeycloakId(userId);

        // Assert
        assertTrue(exists);
        verify(doctorRepository, times(1)).existsByKeycloakUserId(userId);
    }

    /**
     * Test existsByKeycloakId returns false when doctor does not exist.
     */
    @Test
    void existsByKeycloakId_ReturnsFalse() {
        // Arrange
        String userId = "doctor-999";
        when(doctorRepository.existsByKeycloakUserId(userId)).thenReturn(false);

        // Act
        boolean exists = doctorService.existsByKeycloakId(userId);

        // Assert
        assertFalse(exists);
        verify(doctorRepository, times(1)).existsByKeycloakUserId(userId);
    }

    /**
     * Test deleting a doctor by KeycloakUserId successfully.
     */
    @Test
    void deleteByKeycloakUserId_Success() {
        // Arrange
        String userId = "doctor-123";
        doNothing().when(doctorRepository).deleteByKeycloakUserId(userId);

        // Act
        doctorService.deleteByKeycloakUserId(userId);

        // Assert
        verify(doctorRepository, times(1)).deleteByKeycloakUserId(userId);
    }

    /**
     * Test creating a doctor successfully.
     */
    @Test
    void createDoctor_Success() {
        // Arrange
        when(doctorRepository.save(newDoctor)).thenReturn(newDoctor);

        // Act
        Doctor result = doctorService.createDoctor(newDoctor);

        // Assert
        assertNotNull(result);
        assertEquals(newDoctor, result);
        verify(doctorRepository, times(1)).save(newDoctor);
    }

    /**
     * Test updating a doctor successfully.
     */
    @Test
    void updateDoctor_Success() {
        // Arrange
        Long doctorId = 1L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(existingDoctor));
        when(doctorRepository.save(existingDoctor)).thenReturn(existingDoctor);

        // Act
        Doctor result = doctorService.updateDoctor(doctorId, updatedDoctor);

        // Assert
        assertNotNull(result);
        assertEquals(updatedDoctor.getName(), result.getName());
        assertEquals(updatedDoctor.getKeycloakUserId(), result.getKeycloakUserId());
        assertEquals(updatedDoctor.getSpecialties(), result.getSpecialties());
        assertEquals(updatedDoctor.isPrimaryCare(), result.isPrimaryCare());
        verify(doctorRepository, times(1)).findById(doctorId);
        verify(doctorRepository, times(1)).save(existingDoctor);
    }

    /**
     * Test updating a doctor fails when doctor not found.
     */
    @Test
    void updateDoctor_Failure_DoctorNotFound() {
        // Arrange
        Long doctorId = 999L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctor(doctorId, updatedDoctor);
        });

        assertEquals("Doctor not found with id: " + doctorId, exception.getMessage());
        verify(doctorRepository, times(1)).findById(doctorId);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * Test deleting a doctor successfully by ID.
     */
    @Test
    void deleteDoctor_Success() {
        // Arrange
        Long doctorId = 1L;
        doNothing().when(doctorRepository).deleteById(doctorId);

        // Act
        doctorService.deleteDoctor(doctorId);

        // Assert
        verify(doctorRepository, times(1)).deleteById(doctorId);
    }

    /**
     * Test finding all doctors successfully.
     */
    @Test
    void findAll_Success() {
        // Arrange
        List<Doctor> doctors = Arrays.asList(existingDoctor, anotherDoctor);
        when(doctorRepository.findAll()).thenReturn(doctors);

        // Act
        List<Doctor> result = doctorService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains(existingDoctor));
        assertTrue(result.contains(anotherDoctor));
        verify(doctorRepository, times(1)).findAll();
    }

    /**
     * Test finding a doctor by ID successfully.
     */
    @Test
    void findById_Success() {
        // Arrange
        Long doctorId = 1L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.of(existingDoctor));

        // Act
        Doctor result = doctorService.findById(doctorId);

        // Assert
        assertNotNull(result);
        assertEquals(existingDoctor, result);
        verify(doctorRepository, times(1)).findById(doctorId);
    }

    /**
     * Test finding a doctor by ID fails when not found.
     */
    @Test
    void findById_Failure_DoctorNotFound() {
        // Arrange
        Long doctorId = 999L;
        when(doctorRepository.findById(doctorId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.findById(doctorId);
        });

        assertEquals("Doctor not found", exception.getMessage());
        verify(doctorRepository, times(1)).findById(doctorId);
    }

    /**
     * Test finding a doctor by principal successfully.
     */
    @Test
    void findByPrincipal_Success() {
        // Arrange
        String keycloakUserId = "doctor-123";
        User currentUser = new User();
        currentUser.setKeycloakUserId(keycloakUserId);
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(doctorRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(existingDoctor);

        // Act
        Doctor result = doctorService.findByPrincipal();

        // Assert
        assertNotNull(result);
        assertEquals(existingDoctor, result);
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorRepository, times(1)).findByKeycloakUserId(keycloakUserId);
    }

    /**
     * Test finding a doctor by principal fails when doctor not found.
     */
    @Test
    void findByPrincipal_Failure_DoctorNotFound() {
        // Arrange
        String keycloakUserId = "doctor-999";
        User currentUser = new User();
        currentUser.setKeycloakUserId(keycloakUserId);
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(doctorRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(null);

        // Act
        Doctor result = doctorService.findByPrincipal();

        // Assert
        assertNull(result);
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorRepository, times(1)).findByKeycloakUserId(keycloakUserId);
    }

    /**
     * Test updating a doctor by KeycloakUserId successfully.
     */
    @Test
    void updateDoctorByKeycloakUserId_Success() {
        // Arrange
        String keycloakUserId = "doctor-123";
        User user = existingUser; // Assume existingUser corresponds to doctor-123
        user.setFirstName("Alice");
        user.setLastName("Wonderland");

        Doctor updatedInfo = new Doctor();
        updatedInfo.setPrimaryCare(true);
        updatedInfo.setSpecialties("Oncology");

        when(userService.findByKeycloakUserId(keycloakUserId)).thenReturn(user);
        when(doctorRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(existingDoctor);
        when(doctorRepository.save(existingDoctor)).thenReturn(existingDoctor);

        // Act
        Doctor result = doctorService.updateDoctorByKeycloakUserId(keycloakUserId, updatedInfo);

        // Assert
        assertNotNull(result);
        assertEquals("Alice Wonderland", result.getName());
        assertTrue(result.isPrimaryCare());
        assertEquals("Oncology", result.getSpecialties());
        verify(userService, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, times(1)).save(existingDoctor);
    }

    /**
     * Test updating a doctor by KeycloakUserId fails when user not found.
     */
    @Test
    void updateDoctorByKeycloakUserId_Failure_UserNotFound() {
        // Arrange
        String keycloakUserId = "doctor-999";
        Doctor updatedInfo = new Doctor();
        updatedInfo.setPrimaryCare(true);
        updatedInfo.setSpecialties("Oncology");

        when(userService.findByKeycloakUserId(keycloakUserId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorByKeycloakUserId(keycloakUserId, updatedInfo);
        });

        assertEquals("User not found with keycloakUserId: " + keycloakUserId, exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, never()).findByKeycloakUserId(anyString());
        verify(doctorRepository, never()).save(any(Doctor.class));
    }

    /**
     * Test updating a doctor by KeycloakUserId fails when doctor not found.
     */
    @Test
    void updateDoctorByKeycloakUserId_Failure_DoctorNotFound() {
        // Arrange
        String keycloakUserId = "doctor-123";
        User user = existingUser;
        user.setFirstName("Alice");
        user.setLastName("Wonderland");

        Doctor updatedInfo = new Doctor();
        updatedInfo.setPrimaryCare(true);
        updatedInfo.setSpecialties("Oncology");

        when(userService.findByKeycloakUserId(keycloakUserId)).thenReturn(user);
        when(doctorRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(null);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            doctorService.updateDoctorByKeycloakUserId(keycloakUserId, updatedInfo);
        });

        assertEquals("Doctor not found with keycloakUserId: " + keycloakUserId, exception.getMessage());
        verify(userService, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        verify(doctorRepository, never()).save(any(Doctor.class));
    }
}
