package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import bg.nbu.medicalrecords.exception.LocalSyncException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class LocalSyncServiceTest {

    @Mock
    private DoctorService doctorService;

    @Mock
    private PatientService patientService;

    @Mock
    private KeycloakService keycloakService;

    @Mock
    private UserService userService;

    @InjectMocks
    private LocalSyncService localSyncService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void handleRoleChange_ShouldCreateDoctor_WhenNewRoleIsDoctor() {
        // Arrange
        String userId = "123";
        String newRole = "doctor";
        KeycloakUserDto kcData = new KeycloakUserDto();
        kcData.setUsername("testuser");
        kcData.setFirstName("John");
        kcData.setLastName("Doe");

        when(keycloakService.findUserById(userId)).thenReturn(kcData);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(false);
        when(patientService.existsByKeycloakId(userId)).thenReturn(false);

        // Act
        localSyncService.handleRoleChange(userId, newRole);

        // Assert
        verify(doctorService).createDoctorFromKeycloak(userId, "John Doe", userId);
        verify(patientService, never()).createPatientFromKeycloak(anyString(), anyString());
        verify(userService, never()).assignRole(anyString(), anyString());
    }

    @Test
    void handleRoleChange_ShouldCreatePatient_WhenNewRoleIsPatient() {
        // Arrange
        String userId = "456";
        String newRole = "patient";
        KeycloakUserDto kcData = new KeycloakUserDto();
        kcData.setUsername("testuser");
        kcData.setFirstName("Jane");
        kcData.setLastName("Doe");

        when(keycloakService.findUserById(userId)).thenReturn(kcData);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(false);
        when(patientService.existsByKeycloakId(userId)).thenReturn(false);

        // Act
        localSyncService.handleRoleChange(userId, newRole);

        // Assert
        verify(patientService).createPatientFromKeycloak(userId, "Jane Doe");
        verify(doctorService, never()).createDoctorFromKeycloak(anyString(), anyString(), anyString());
        verify(userService, never()).assignRole(anyString(), anyString());
    }

    @Test
    void handleRoleChange_ShouldAssignAdminRole_WhenNewRoleIsAdmin() {
        // Arrange
        String userId = "789";
        String newRole = "admin";
        KeycloakUserDto kcData = new KeycloakUserDto();
        kcData.setUsername("adminuser");

        when(keycloakService.findUserById(userId)).thenReturn(kcData);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(false);
        when(patientService.existsByKeycloakId(userId)).thenReturn(false);

        // Act
        localSyncService.handleRoleChange(userId, newRole);

        // Assert
        verify(userService).assignRole(userId, "admin");
        verify(doctorService, never()).createDoctorFromKeycloak(anyString(), anyString(), anyString());
        verify(patientService, never()).createPatientFromKeycloak(anyString(), anyString());
    }

    @Test
    void handleRoleChange_ShouldThrowLocalSyncException_OnError() {
        // Arrange
        String userId = "123";
        String newRole = "doctor";

        when(keycloakService.findUserById(userId)).thenThrow(new RuntimeException("Keycloak error"));

        // Act & Assert
        LocalSyncException exception = assertThrows(LocalSyncException.class, () ->
                localSyncService.handleRoleChange(userId, newRole));

        assertTrue(exception.getMessage().contains("Failed to handle role change for user ID: " + userId));
        verify(doctorService, never()).createDoctorFromKeycloak(anyString(), anyString(), anyString());
        verify(patientService, never()).createPatientFromKeycloak(anyString(), anyString());
        verify(userService, never()).assignRole(anyString(), anyString());
    }

    @Test
    void handleRoleChange_ShouldDeleteExistingRecords_BeforeCreatingNewOnes() {
        // Arrange
        String userId = "123";
        String newRole = "doctor";
        KeycloakUserDto kcData = new KeycloakUserDto();
        kcData.setUsername("testuser");
        kcData.setFirstName("John");
        kcData.setLastName("Doe");

        when(keycloakService.findUserById(userId)).thenReturn(kcData);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(true);
        when(patientService.existsByKeycloakId(userId)).thenReturn(true);

        // Act
        localSyncService.handleRoleChange(userId, newRole);

        // Assert
        verify(doctorService).deleteByKeycloakUserId(userId);
        verify(patientService).deleteByKeycloakUserId(userId);
        verify(doctorService).createDoctorFromKeycloak(userId, "John Doe", userId);
    }
}
