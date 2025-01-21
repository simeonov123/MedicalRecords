package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.CreatePrescriptionDto;
import bg.nbu.medicalrecords.dto.UpdatePrescriptionDto;
import bg.nbu.medicalrecords.exception.*;
import bg.nbu.medicalrecords.repository.PrescriptionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PrescriptionServiceTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PrescriptionRepository prescriptionRepository;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private TreatmentService treatmentService;

    @Mock
    private MedicationService medicationService;

    @Mock
    private DiagnosisService diagnosisService;

    @InjectMocks
    private PrescriptionService prescriptionService;

    @Test
    void createPrescription_Success() {
        // Arrange
        Long appointmentId = 1L;
        Long treatmentId = 2L;
        Long medicationId = 3L;
        CreatePrescriptionDto createPrescriptionDto = new CreatePrescriptionDto(medicationId, "1 tablet twice a day", 7);

        User currentUser = new User();
        currentUser.setKeycloakUserId("doctor-1");
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setKeycloakUserId("doctor-1");

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setDoctor(doctor);

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(5L);
        diagnosis.setTreatments(new ArrayList<>());

        Treatment treatment = new Treatment();
        treatment.setId(treatmentId);
        treatment.setDiagnosis(diagnosis); // Associate diagnosis
        treatment.setPrescriptions(new ArrayList<>());
        diagnosis.getTreatments().add(treatment);

        Medication medication = new Medication();
        medication.setId(medicationId);

        Prescription prescription = new Prescription();
        prescription.setId(4L);
        prescription.setDosage(createPrescriptionDto.getDosage());
        prescription.setDuration(createPrescriptionDto.getDuration());
        prescription.setTreatment(treatment);
        prescription.setMedication(medication);

        // Mock service calls
        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(treatmentService.findById(treatmentId)).thenReturn(treatment);
        when(medicationService.findById(medicationId)).thenReturn(medication);
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);
        when(treatmentService.save(any(Treatment.class))).thenReturn(treatment);
        when(diagnosisService.save(any(Diagnosis.class))).thenReturn(diagnosis);

        // Act
        Prescription result = prescriptionService.createPrescription(appointmentId, treatmentId, createPrescriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals(createPrescriptionDto.getDosage(), result.getDosage());
        assertEquals(createPrescriptionDto.getDuration(), result.getDuration());
        assertEquals(medicationId, result.getMedication().getId());
        assertEquals(treatmentId, result.getTreatment().getId());
        verify(appointmentService, times(1)).save(appointment);
        verify(treatmentService, times(1)).save(treatment);
        verify(diagnosisService, times(1)).save(diagnosis);
    }




    @Test
    void createPrescription_DoctorNotAssigned() {
        // Arrange
        Long appointmentId = 1L;
        Long treatmentId = 2L;
        Long medicationId = 3L;
        CreatePrescriptionDto createPrescriptionDto = new CreatePrescriptionDto(medicationId, "1 tablet twice a day", 7);

        User currentUser = new User();
        currentUser.setKeycloakUserId("doctor-2");
        currentUser.setRole("doctor");

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        Doctor doctor = new Doctor();
        doctor.setKeycloakUserId("doctor-1");
        appointment.setDoctor(doctor);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);

        // Act & Assert
        assertThrows(DoctorNotAssignedException.class, () ->
                prescriptionService.createPrescription(appointmentId, treatmentId, createPrescriptionDto));
    }

    @Test
    void updatePrescription_Success() {
        // Arrange
        Long appointmentId = 1L;
        Long treatmentId = 2L;
        Long prescriptionId = 4L;
        UpdatePrescriptionDto updatePrescriptionDto = new UpdatePrescriptionDto(3L, "2 tablets daily", 10);

        User currentUser = new User();
        currentUser.setKeycloakUserId("doctor-1");
        currentUser.setRole("doctor");

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        Doctor doctor = new Doctor();
        doctor.setKeycloakUserId("doctor-1");
        appointment.setDoctor(doctor);

        Treatment treatment = new Treatment();
        treatment.setId(treatmentId);

        Prescription prescription = new Prescription();
        prescription.setId(prescriptionId);
        prescription.setDosage("1 tablet twice a day");
        prescription.setDuration(7);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(treatmentService.findById(treatmentId)).thenReturn(treatment);
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenReturn(prescription);

        // Act
        Prescription result = prescriptionService.updatePrescription(appointmentId, treatmentId, prescriptionId, updatePrescriptionDto);

        // Assert
        assertNotNull(result);
        assertEquals(updatePrescriptionDto.getDosage(), result.getDosage());
        assertEquals(updatePrescriptionDto.getDuration(), result.getDuration());
        verify(appointmentService, times(1)).save(appointment);
    }

    @Test
    void deletePrescription_Success() {
        // Arrange
        Long appointmentId = 1L;
        Long treatmentId = 2L;
        Long prescriptionId = 4L;

        User currentUser = new User();
        currentUser.setKeycloakUserId("doctor-1");
        currentUser.setRole("doctor");

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        Doctor doctor = new Doctor();
        doctor.setKeycloakUserId("doctor-1");
        appointment.setDoctor(doctor);

        Treatment treatment = new Treatment();
        treatment.setId(treatmentId);

        Prescription prescription = new Prescription();
        prescription.setId(prescriptionId);
        prescription.setTreatment(treatment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentService.findById(appointmentId)).thenReturn(appointment);
        when(treatmentService.findById(treatmentId)).thenReturn(treatment);
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(prescription));

        // Act
        prescriptionService.deletePrescription(appointmentId, treatmentId, prescriptionId);

        // Assert
        verify(prescriptionRepository, times(1)).delete(prescription);
        verify(appointmentService, times(1)).save(appointment);
    }

    @Test
    void deletePrescription_NotFound() {
        // Arrange
        Long appointmentId = 1L;
        Long treatmentId = 2L;
        Long prescriptionId = 4L;

        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(PrescriptionNotFoundException.class, () ->
                prescriptionService.deletePrescription(appointmentId, treatmentId, prescriptionId));
    }
}