package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreateDiagnosisDto;
import bg.nbu.medicalrecords.dto.UpdateDiagnosisDto;
import bg.nbu.medicalrecords.repository.DiagnosisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class DiagnosisServiceTest {

    @Mock
    private DiagnosisRepository diagnosisRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private DoctorService doctorService;

    @InjectMocks
    private DiagnosisService diagnosisService;

    private User doctorUser;
    private User anotherDoctorUser;
    private Doctor doctor;
    private Doctor anotherDoctor;
    private Patient patient;
    private Appointment appointment;
    private Diagnosis diagnosis;

    @BeforeEach
    void setUp() {
        // Initialize common objects
        doctorUser = new User();
        doctorUser.setId(1L);
        doctorUser.setKeycloakUserId("doctor-123");
        doctorUser.setRole("doctor");

        anotherDoctorUser = new User();
        anotherDoctorUser.setId(2L);
        anotherDoctorUser.setKeycloakUserId("doctor-456");
        anotherDoctorUser.setRole("doctor");

        doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("doctor-123");
        doctor.setName("Dr. Smith");

        anotherDoctor = new Doctor();
        anotherDoctor.setId(2L);
        anotherDoctor.setKeycloakUserId("doctor-456");
        anotherDoctor.setName("Dr. Johnson");

        patient = new Patient();
        patient.setId(3L);
        patient.setKeycloakUserId("patient-789");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(doctor);

        appointment = new Appointment();
        appointment.setId(4L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 5, 20, 10, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 5, 20, 9, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 5, 20, 9, 0));

        diagnosis = new Diagnosis();
        diagnosis.setId(5L);
        diagnosis.setAppointment(appointment);
        diagnosis.setStatement("Common Cold");
        diagnosis.setDiagnosedDate(LocalDateTime.of(2025, 5, 20, 10, 30));
    }

    /**
     * Test successfully creating a diagnosis by the assigned doctor.
     */
    @Test
    void createDiagnosis_Success_AssignedDoctor() {
        // Arrange
        CreateDiagnosisDto createDiagnosisDto = new CreateDiagnosisDto();
        createDiagnosisDto.setStatement("Flu");
        createDiagnosisDto.setDiagnosedDate(LocalDateTime.of(2025, 5, 20, 11, 0));

        when(authenticationService.getCurrentUser()).thenReturn(doctorUser);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);
        when(diagnosisRepository.save(any(Diagnosis.class))).thenReturn(diagnosis);
        doNothing().when(appointmentService).save(appointment);

        // Act
        Diagnosis result = diagnosisService.createDiagnosis(appointment.getId(), createDiagnosisDto);

        // Assert
        assertNotNull(result);
        assertEquals(diagnosis, result);
        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, times(1)).save(any(Diagnosis.class));
        verify(appointmentService, times(1)).save(appointment);
    }

    /**
     * Test creating a diagnosis by a doctor not assigned to the appointment.
     */
    @Test
    void createDiagnosis_Failure_DoctorNotAssigned() {
        // Arrange
        CreateDiagnosisDto createDiagnosisDto = new CreateDiagnosisDto();
        createDiagnosisDto.setStatement("Flu");
        createDiagnosisDto.setDiagnosedDate(LocalDateTime.of(2025, 5, 20, 11, 0));

        when(authenticationService.getCurrentUser()).thenReturn(anotherDoctorUser);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.createDiagnosis(appointment.getId(), createDiagnosisDto);
        });

        assertEquals("Doctor is not assigned to this appointment", exception.getMessage());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, never()).save(any(Diagnosis.class));
        verify(appointmentService, never()).save(any(Appointment.class));
    }

    /**
     * Test updating a diagnosis successfully by the assigned doctor.
     */
    @Test
    void updateDiagnosis_Success_AssignedDoctor() {
        // Arrange
        UpdateDiagnosisDto updateDiagnosisDto = new UpdateDiagnosisDto("Severe Flu", LocalDateTime.of(2025, 5, 20, 12, 0));

        when(authenticationService.getCurrentUser()).thenReturn(doctorUser);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.of(diagnosis));
        when(diagnosisRepository.save(any(Diagnosis.class))).thenReturn(diagnosis);
        doNothing().when(appointmentService).save(appointment);

        // Act
        Diagnosis result = diagnosisService.updateDiagnosis(appointment.getId(), diagnosis.getId(), updateDiagnosisDto);

        // Assert
        assertNotNull(result);
        assertEquals("Severe Flu", result.getStatement());
        assertEquals(LocalDateTime.of(2025, 5, 20, 12, 0), result.getDiagnosedDate());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
        verify(diagnosisRepository, times(1)).save(diagnosis);
        verify(appointmentService, times(1)).save(appointment);
    }

    /**
     * Test updating a diagnosis by a doctor not assigned to the appointment.
     */
    @Test
    void updateDiagnosis_Failure_DoctorNotAssigned() {
        // Arrange
        UpdateDiagnosisDto updateDiagnosisDto = new UpdateDiagnosisDto("Severe Flu", LocalDateTime.of(2025, 5, 20, 12, 0));

        when(authenticationService.getCurrentUser()).thenReturn(anotherDoctorUser);
        when(doctorService.findByPrincipal()).thenReturn(anotherDoctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.updateDiagnosis(appointment.getId(), diagnosis.getId(), updateDiagnosisDto);
        });

        assertEquals("Doctor is not assigned to this appointment", exception.getMessage());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, never()).findById(anyLong());
        verify(diagnosisRepository, never()).save(any(Diagnosis.class));
        verify(appointmentService, never()).save(any(Appointment.class));
    }

    /**
     * Test updating a diagnosis that does not exist.
     */
    @Test
    void updateDiagnosis_Failure_DiagnosisNotFound() {
        // Arrange
        UpdateDiagnosisDto updateDiagnosisDto = new UpdateDiagnosisDto("Severe Flu", LocalDateTime.of(2025, 5, 20, 12, 0));

        when(authenticationService.getCurrentUser()).thenReturn(doctorUser);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.updateDiagnosis(appointment.getId(), diagnosis.getId(), updateDiagnosisDto);
        });

        assertEquals("Sick leave not found", exception.getMessage());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
        verify(diagnosisRepository, never()).save(any(Diagnosis.class));
        verify(appointmentService, never()).save(any(Appointment.class));
    }

    /**
     * Test deleting a diagnosis successfully by the assigned doctor.
     */
    @Test
    void deleteDiagnosis_Success_AssignedDoctor() {
        // Arrange
        when(authenticationService.getCurrentUser()).thenReturn(doctorUser);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.of(diagnosis));
        doNothing().when(diagnosisRepository).delete(diagnosis);
        doNothing().when(appointmentService).save(appointment);

        // Act
        diagnosisService.deleteDiagnosis(diagnosis.getId(), appointment.getId());

        // Assert
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
        verify(diagnosisRepository, times(1)).delete(diagnosis);
        verify(appointmentService, times(1)).save(appointment);
    }

    /**
     * Test deleting a diagnosis by a doctor not assigned to the appointment.
     */
    @Test
    void deleteDiagnosis_Failure_DoctorNotAssigned() {
        // Arrange
        when(authenticationService.getCurrentUser()).thenReturn(anotherDoctorUser);
        when(doctorService.findByPrincipal()).thenReturn(anotherDoctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.deleteDiagnosis(diagnosis.getId(), appointment.getId());
        });

        assertEquals("Doctor is not assigned to this appointment", exception.getMessage());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, never()).findById(anyLong());
        verify(diagnosisRepository, never()).delete(any(Diagnosis.class));
        verify(appointmentService, never()).save(any(Appointment.class));
    }

    /**
     * Test deleting a diagnosis that does not exist.
     */
    @Test
    void deleteDiagnosis_Failure_DiagnosisNotFound() {
        // Arrange
        when(authenticationService.getCurrentUser()).thenReturn(doctorUser);
        when(doctorService.findByPrincipal()).thenReturn(doctor);
        when(appointmentService.findById(appointment.getId())).thenReturn(appointment);
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.deleteDiagnosis(diagnosis.getId(), appointment.getId());
        });

        assertEquals("Diagnosis not found", exception.getMessage());
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentService, times(1)).findById(appointment.getId());
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
        verify(diagnosisRepository, never()).delete(any(Diagnosis.class));
        verify(appointmentService, never()).save(any(Appointment.class));
    }

    /**
     * Test finding a diagnosis by its ID successfully.
     */
    @Test
    void findById_Success() {
        // Arrange
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.of(diagnosis));

        // Act
        Diagnosis result = diagnosisService.findById(diagnosis.getId());

        // Assert
        assertNotNull(result);
        assertEquals(diagnosis, result);
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
    }

    /**
     * Test finding a diagnosis by its ID when it does not exist.
     */
    @Test
    void findById_Failure_DiagnosisNotFound() {
        // Arrange
        when(diagnosisRepository.findById(diagnosis.getId())).thenReturn(Optional.empty());

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            diagnosisService.findById(diagnosis.getId());
        });

        assertEquals("Diagnosis not found", exception.getMessage());
        verify(diagnosisRepository, times(1)).findById(diagnosis.getId());
    }

    /**
     * Test saving a diagnosis.
     */
    @Test
    void save_Success() {
        // Arrange
        when(diagnosisRepository.save(diagnosis)).thenReturn(diagnosis);

        // Act
        Diagnosis result = diagnosisService.save(diagnosis);

        // Assert
        assertNotNull(result);
        assertEquals(diagnosis, result);
        verify(diagnosisRepository, times(1)).save(diagnosis);
    }

    /**
     * Test retrieving unique diagnosis statements.
     */
    @Test
    void getUniqueDiagnosis_Success() {
        // Arrange
        List<String> uniqueDiagnoses = Arrays.asList("Flu", "Common Cold");
        when(diagnosisRepository.findDistinctStatements()).thenReturn(uniqueDiagnoses);

        // Act
        List<String> result = diagnosisService.getUniqueDiagnosis();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("Flu"));
        assertTrue(result.contains("Common Cold"));
        verify(diagnosisRepository, times(1)).findDistinctStatements();
    }

    /**
     * Test finding diagnoses by statement.
     */
    @Test
    void findByStatement_Success() {
        // Arrange
        String statement = "Flu";
        List<Diagnosis> diagnoses = Arrays.asList(diagnosis);
        when(diagnosisRepository.findByStatementIgnoreCase(statement)).thenReturn(diagnoses);

        // Act
        List<Diagnosis> result = diagnosisService.findByStatement(statement);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(diagnosis, result.get(0));
        verify(diagnosisRepository, times(1)).findByStatementIgnoreCase(statement);
    }

    /**
     * Test counting the total number of diagnoses.
     */
    @Test
    void count_Success() {
        // Arrange
        long count = 10L;
        when(diagnosisRepository.count()).thenReturn(count);

        // Act
        long result = diagnosisService.count();

        // Assert
        assertEquals(count, result);
        verify(diagnosisRepository, times(1)).count();
    }
}
