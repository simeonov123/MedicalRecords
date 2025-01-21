package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreateTreatmentDto;
import bg.nbu.medicalrecords.dto.TreatmentDto;
import bg.nbu.medicalrecords.dto.UpdateTreatmentDto;
import bg.nbu.medicalrecords.exception.DoctorNotAssignedToAppointmentException;
import bg.nbu.medicalrecords.exception.TreatmentNotFoundException;
import bg.nbu.medicalrecords.repository.TreatmentRepository;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TreatmentService.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class TreatmentServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private DiagnosisService diagnosisService;

    @Mock
    private TreatmentRepository treatmentRepository;

    @InjectMocks
    private TreatmentService treatmentService;

    @BeforeEach
    void setUp() {
        // Initialize common objects if needed
    }


    /**
     * Test failure when creating a treatment by a doctor not assigned to the appointment.
     */
    @Test
    void createTreatment_Failure_DoctorNotAssigned() {
        Long appointmentId = 1L;
        Long diagnosisId = 10L;
        CreateTreatmentDto createTreatmentDto = new CreateTreatmentDto(
                LocalDate.now(),
                LocalDate.now().plusDays(10),
                "Physical Therapy"
        );

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("kc-doctor-1");

        User currentUser = new User();
        currentUser.setKeycloakUserId("kc-doctor-2");
        currentUser.setRole("doctor");

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);
        appointment.setDiagnoses(new ArrayList<>());

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);

        DoctorNotAssignedToAppointmentException exception = assertThrows(DoctorNotAssignedToAppointmentException.class, () -> {
            treatmentService.createTreatment(appointmentId, diagnosisId, createTreatmentDto);
        });

        assertEquals("Doctor is not assigned to this appointment", exception.getMessage());

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentService, times(1)).findById(appointmentId);
        verify(diagnosisService, never()).findById(anyLong());
        verify(treatmentRepository, never()).save(any(Treatment.class));
    }

    /**
     * Test successfully finding a treatment by ID.
     */
    @Test
    void findById_Success() {
        Long treatmentId = 100L;

        Treatment treatment = new Treatment();
        treatment.setId(treatmentId);
        treatment.setDescription("Physical Therapy");
        treatment.setStartDate(LocalDate.now());
        treatment.setEndDate(LocalDate.now().plusDays(10));

        when(treatmentRepository.findById(treatmentId)).thenReturn(Optional.of(treatment));

        Treatment result = treatmentService.findById(treatmentId);

        assertNotNull(result);
        assertEquals(treatmentId, result.getId());
        assertEquals("Physical Therapy", result.getDescription());

        verify(treatmentRepository, times(1)).findById(treatmentId);
    }

    /**
     * Test failure when finding a treatment by ID.
     */
    @Test
    void findById_Failure_TreatmentNotFound() {
        Long treatmentId = 100L;
        when(treatmentRepository.findById(treatmentId)).thenReturn(Optional.empty());

        TreatmentNotFoundException exception = assertThrows(TreatmentNotFoundException.class, () -> {
            treatmentService.findById(treatmentId);
        });

        assertEquals("Treatment not found", exception.getMessage());
        verify(treatmentRepository, times(1)).findById(treatmentId);
    }

    /**
     * Test successfully saving a treatment.
     */
    @Test
    void save_Success() {
        Treatment treatment = new Treatment();
        treatment.setId(100L);
        treatment.setDescription("Physical Therapy");
        treatment.setStartDate(LocalDate.now());
        treatment.setEndDate(LocalDate.now().plusDays(10));

        Treatment savedTreatment = new Treatment();
        savedTreatment.setId(100L);
        savedTreatment.setDescription("Physical Therapy Updated");
        savedTreatment.setStartDate(treatment.getStartDate());
        savedTreatment.setEndDate(treatment.getEndDate());
        savedTreatment.setUpdatedAt(LocalDateTime.now());

        when(treatmentRepository.save(treatment)).thenReturn(savedTreatment);

        Treatment result = treatmentService.save(treatment);

        assertNotNull(result);
        assertEquals("Physical Therapy Updated", result.getDescription());
        assertNotNull(result.getUpdatedAt());

        verify(treatmentRepository, times(1)).save(treatment);
    }
}
