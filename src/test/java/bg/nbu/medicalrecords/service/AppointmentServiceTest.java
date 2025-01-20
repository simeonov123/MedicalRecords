package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.Appointment;
import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.domain.Patient;
import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.repository.AppointmentRepository;
import bg.nbu.medicalrecords.repository.PatientRepository;
import bg.nbu.medicalrecords.util.MappingUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;

import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private PatientService patientService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AppointmentService appointmentService;

    /**
     * Test retrieving all appointments for the logged-in user when the user has the "doctor" role.
     */
    @Test
    void testFindAllForLoggedInUser_DoctorRole() {
        // Arrange
        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setKeycloakUserId("doctor-123");
        currentUser.setRole("doctor");

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("doctor-123");
        doctor.setName("Dr. Smith");
        doctor.setSpecialties("Cardiology, Internal Medicine");
        doctor.setPrimaryCare(true);

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(doctor);

        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 1, 15, 10, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 1, 15, 9, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 9, 0));

        List<Appointment> appointments = Collections.singletonList(appointment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentRepository.findByDoctor_KeycloakUserId("doctor-123")).thenReturn(appointments);

        PatientDto patientDto = new PatientDto(
                2L,
                "John Doe",
                "patient-456",
                true,
                1L,
                "patient-456"
        );

        DoctorDto doctorDto = new DoctorDto(
                1L,
                "doctor-123",
                "Dr. Smith",
                "Cardiology, Internal Medicine",
                true
        );

        AppointmentDto appointmentDto = new AppointmentDto(
                3L,
                patientDto,
                doctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 10, 0)
        );

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(appointment, currentUser))
                    .thenReturn(appointmentDto);

            // Act
            List<AppointmentDto> result = appointmentService.findAllForLoggedInUser();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(3L, result.get(0).getId());
            assertEquals("John Doe", result.get(0).getPatient().getName());
            assertEquals("Dr. Smith", result.get(0).getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), result.get(0).getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment, currentUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentRepository, times(1)).findByDoctor_KeycloakUserId("doctor-123");
    }

    /**
     * Test retrieving all appointments for the logged-in user when the user has the "patient" role.
     */
    @Test
    void testFindAllForLoggedInUser_PatientRole() {
        // Arrange
        User currentUser = new User();
        currentUser.setId(2L);
        currentUser.setKeycloakUserId("patient-456");
        currentUser.setRole("patient");

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("doctor-123");
        doctor.setName("Dr. Smith");
        doctor.setSpecialties("Cardiology, Internal Medicine");
        doctor.setPrimaryCare(true);

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(doctor);

        Appointment appointment = new Appointment();
        appointment.setId(3L);
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 1, 15, 10, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 1, 15, 9, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 1, 15, 9, 0));

        List<Appointment> appointments = Collections.singletonList(appointment);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentRepository.findByPatient_KeycloakUserId("patient-456")).thenReturn(appointments);

        PatientDto patientDto = new PatientDto(
                2L,
                "John Doe",
                "patient-456",
                true,
                1L,
                "patient-456"
        );

        DoctorDto doctorDto = new DoctorDto(
                1L,
                "doctor-123",
                "Dr. Smith",
                "Cardiology, Internal Medicine",
                true
        );

        AppointmentDto appointmentDto = new AppointmentDto(
                3L,
                patientDto,
                doctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 10, 0)
        );

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(appointment, currentUser))
                    .thenReturn(appointmentDto);

            // Act
            List<AppointmentDto> result = appointmentService.findAllForLoggedInUser();

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals(3L, result.get(0).getId());
            assertEquals("John Doe", result.get(0).getPatient().getName());
            assertEquals("Dr. Smith", result.get(0).getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 1, 15, 10, 0), result.get(0).getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment, currentUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentRepository, times(1)).findByPatient_KeycloakUserId("patient-456");
    }

    /**
     * Test creating a new appointment.
     */
    @Test
    void testCreateAppointment() {
        // Arrange
        CreateAppointmentDto createDto = new CreateAppointmentDto();
        createDto.setPatientId(2L);
        createDto.setDoctorId(1L);
        createDto.setDate(LocalDateTime.of(2025, 3, 15, 10, 0));

        User currentUser = new User();
        currentUser.setId(3L);
        currentUser.setKeycloakUserId("patient-456");
        currentUser.setRole("patient");

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(null); // Initially null

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("doctor-123");
        doctor.setName("Dr. Smith");
        doctor.setSpecialties("Cardiology, Internal Medicine");
        doctor.setPrimaryCare(true);

        Appointment savedAppointment = new Appointment();
        savedAppointment.setId(4L);
        savedAppointment.setPatient(patient);
        savedAppointment.setDoctor(doctor);
        savedAppointment.setAppointmentDateTime(LocalDateTime.of(2025, 3, 15, 10, 0));
        savedAppointment.setCreatedAt(LocalDateTime.of(2025, 3, 15, 9, 0));
        savedAppointment.setUpdatedAt(LocalDateTime.of(2025, 3, 15, 9, 0));

        PatientDto patientDto = new PatientDto(
                2L,
                "John Doe",
                "patient-456",
                true,
                null, // primaryDoctorId is null
                "patient-456"
        );

        DoctorDto doctorDto = new DoctorDto(
                1L,
                "doctor-123",
                "Dr. Smith",
                "Cardiology, Internal Medicine",
                true
        );

        AppointmentDto appointmentDto = new AppointmentDto(
                4L,
                patientDto,
                doctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 3, 15, 9, 0),
                LocalDateTime.of(2025, 3, 15, 9, 0),
                LocalDateTime.of(2025, 3, 15, 10, 0)
        );

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(patientService.findPatientById(2L)).thenReturn(patient);
        when(doctorService.findById(1L)).thenReturn(doctor);
        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(savedAppointment, currentUser))
                    .thenReturn(appointmentDto);

            // Act
            AppointmentDto result = appointmentService.createAppointment(createDto);

            // Assert
            assertNotNull(result);
            assertEquals(4L, result.getId());
            assertEquals("John Doe", result.getPatient().getName());
            assertEquals("Dr. Smith", result.getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 3, 15, 10, 0), result.getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(savedAppointment, currentUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(patientService, times(1)).findPatientById(2L);
        verify(doctorService, times(1)).findById(1L);
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    /**
     * Test finding an appointment by its ID successfully.
     */
    @Test
    void testFindById_Success() {
        // Arrange
        Long appointmentId = 1L;
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 4, 10, 15, 0));

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        // Act
        Appointment result = appointmentService.findById(appointmentId);

        // Assert
        assertNotNull(result);
        assertEquals(appointmentId, result.getId());
        assertEquals(LocalDateTime.of(2025, 4, 10, 15, 0), result.getAppointmentDateTime());

        verify(appointmentRepository, times(1)).findById(appointmentId);
    }

    /**
     * Test finding an appointment by its ID when it does not exist.
     */
    @Test
    void testFindById_NotFound() {
        // Arrange
        Long appointmentId = 1L;

        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            appointmentService.findById(appointmentId);
        });

        assertEquals("Appointment not found", exception.getMessage());

        verify(appointmentRepository, times(1)).findById(appointmentId);
    }

    /**
     * Test updating an appointment when the current user is a doctor assigned to the appointment.
     */
    @Test
    void testUpdateAppointment_DoctorRole() {
        // Arrange
        Long appointmentId = 1L;
        UpdateAppointmentDto updateDto = new UpdateAppointmentDto(
                null, // doctorId is not being updated by doctor
                LocalDateTime.of(2025, 4, 10, 15, 0)
        );

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setKeycloakUserId("doctor-123");
        currentUser.setRole("doctor");

        Doctor currentDoctor = new Doctor();
        currentDoctor.setId(2L);
        currentDoctor.setKeycloakUserId("doctor-123");
        currentDoctor.setName("Dr. Johnson");
        currentDoctor.setSpecialties("Neurology");
        currentDoctor.setPrimaryCare(false);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(currentDoctor);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatient(patient);
        appointment.setDoctor(currentDoctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 4, 10, 15, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setId(appointmentId);
        updatedAppointment.setPatient(patient);
        updatedAppointment.setDoctor(currentDoctor); // No change
        updatedAppointment.setAppointmentDateTime(updateDto.getAppointmentDateTime());
        updatedAppointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        updatedAppointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        PatientDto patientDto = new PatientDto(
                1L,
                "John Doe",
                "patient-456",
                true,
                2L,
                "patient-456"
        );

        DoctorDto doctorDto = new DoctorDto(
                2L,
                "doctor-123",
                "Dr. Johnson",
                "Neurology",
                false
        );

        AppointmentDto appointmentDto = new AppointmentDto(
                appointmentId,
                patientDto,
                doctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 15, 0)
        );

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(doctorService.findByPrincipal()).thenReturn(currentDoctor);
        when(appointmentRepository.save(appointment)).thenReturn(updatedAppointment);

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(updatedAppointment, currentUser))
                    .thenReturn(appointmentDto);

            // Act
            AppointmentDto result = appointmentService.updateAppointment(appointmentId, updateDto);

            // Assert
            assertNotNull(result);
            assertEquals(appointmentId, result.getId());
            assertEquals("Dr. Johnson", result.getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 4, 10, 15, 0), result.getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(updatedAppointment, currentUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentRepository, times(1)).findById(appointmentId);
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentRepository, times(1)).save(appointment);
    }

    /**
     * Test updating an appointment when the current user is an admin and updates the doctor.
     */
    @Test
    void testUpdateAppointment_AdminRole() {
        // Arrange
        Long appointmentId = 1L;
        UpdateAppointmentDto updateDto = new UpdateAppointmentDto(
                3L, // New doctorId
                LocalDateTime.of(2025, 4, 10, 16, 0)
        );

        User currentUser = new User();
        currentUser.setId(4L);
        currentUser.setKeycloakUserId("admin-789");
        currentUser.setRole("admin");

        Doctor newDoctor = new Doctor();
        newDoctor.setId(3L);
        newDoctor.setKeycloakUserId("doctor-456");
        newDoctor.setName("Dr. Williams");
        newDoctor.setSpecialties("Dermatology");
        newDoctor.setPrimaryCare(false);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(newDoctor);

        Doctor existingDoctor = new Doctor();
        existingDoctor.setId(2L);
        existingDoctor.setKeycloakUserId("doctor-123");
        existingDoctor.setName("Dr. Johnson");
        existingDoctor.setSpecialties("Neurology");
        existingDoctor.setPrimaryCare(false);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatient(patient);
        appointment.setDoctor(existingDoctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 4, 10, 15, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        Appointment updatedAppointment = new Appointment();
        updatedAppointment.setId(appointmentId);
        updatedAppointment.setPatient(patient);
        updatedAppointment.setDoctor(newDoctor);
        updatedAppointment.setAppointmentDateTime(updateDto.getAppointmentDateTime());
        updatedAppointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        updatedAppointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        PatientDto patientDto = new PatientDto(
                1L,
                "John Doe",
                "patient-456",
                true,
                3L,
                "patient-456"
        );

        DoctorDto newDoctorDto = new DoctorDto(
                3L,
                "doctor-456",
                "Dr. Williams",
                "Dermatology",
                false
        );

        AppointmentDto appointmentDto = new AppointmentDto(
                appointmentId,
                patientDto,
                newDoctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 16, 0)
        );

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(doctorService.findById(3L)).thenReturn(newDoctor);
        when(appointmentRepository.save(appointment)).thenReturn(updatedAppointment);

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(updatedAppointment, currentUser))
                    .thenReturn(appointmentDto);

            // Act
            AppointmentDto result = appointmentService.updateAppointment(appointmentId, updateDto);

            // Assert
            assertNotNull(result);
            assertEquals(appointmentId, result.getId());
            assertEquals("Dr. Williams", result.getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 4, 10, 16, 0), result.getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(updatedAppointment, currentUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentRepository, times(1)).findById(appointmentId);
        verify(doctorService, times(1)).findById(3L);
        verify(appointmentRepository, times(1)).save(appointment);
    }

    /**
     * Test deleting an appointment as a doctor who is assigned to the appointment.
     */
    @Test
    void testDeleteAppointment_DoctorRole_Success() {
        // Arrange
        Long appointmentId = 1L;

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setKeycloakUserId("doctor-123");
        currentUser.setRole("doctor");

        Doctor currentDoctor = new Doctor();
        currentDoctor.setId(2L);
        currentDoctor.setKeycloakUserId("doctor-123");
        currentDoctor.setName("Dr. Johnson");
        currentDoctor.setSpecialties("Neurology");
        currentDoctor.setPrimaryCare(false);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(currentDoctor);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatient(patient);
        appointment.setDoctor(currentDoctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 4, 10, 15, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(doctorService.findByPrincipal()).thenReturn(currentDoctor);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        // Act
        appointmentService.deleteAppointment(appointmentId);

        // Assert
        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentRepository, times(1)).findById(appointmentId);
        verify(appointmentRepository, times(1)).delete(appointment);
    }

    /**
     * Test deleting an appointment as a doctor who is NOT assigned to the appointment.
     */
    @Test
    void testDeleteAppointment_DoctorRole_NotAssigned() {
        // Arrange
        Long appointmentId = 1L;

        User currentUser = new User();
        currentUser.setId(1L);
        currentUser.setKeycloakUserId("doctor-123");
        currentUser.setRole("doctor");

        Doctor currentDoctor = new Doctor();
        currentDoctor.setId(3L); // Different doctor
        currentDoctor.setKeycloakUserId("doctor-123");
        currentDoctor.setName("Dr. Williams");
        currentDoctor.setSpecialties("Dermatology");
        currentDoctor.setPrimaryCare(false);

        Doctor appointmentDoctor = new Doctor();
        appointmentDoctor.setId(2L);
        appointmentDoctor.setKeycloakUserId("doctor-456");
        appointmentDoctor.setName("Dr. Johnson");
        appointmentDoctor.setSpecialties("Neurology");
        appointmentDoctor.setPrimaryCare(false);

        Patient patient = new Patient();
        patient.setId(1L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(appointmentDoctor);

        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setPatient(patient);
        appointment.setDoctor(appointmentDoctor);
        appointment.setAppointmentDateTime(LocalDateTime.of(2025, 4, 10, 15, 0));
        appointment.setCreatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));
        appointment.setUpdatedAt(LocalDateTime.of(2025, 4, 10, 14, 0));

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(doctorService.findByPrincipal()).thenReturn(currentDoctor);
        when(appointmentRepository.findById(appointmentId)).thenReturn(Optional.of(appointment));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            appointmentService.deleteAppointment(appointmentId);
        });

        assertEquals("Doctor is not assigned to this appointment", exception.getMessage());

        verify(authenticationService, times(1)).getCurrentUser();
        verify(doctorService, times(1)).findByPrincipal();
        verify(appointmentRepository, times(1)).findById(appointmentId);
        verify(appointmentRepository, never()).delete(any(Appointment.class));
    }

    /**
     * Test retrieving all appointments for a specific patient when the current user is an admin.
     */
    @Test
    void testFindAllForPatient_AdminRole() {
        // Arrange
        Long patientId = 1L;

        User currentUser = new User();
        currentUser.setId(4L);
        currentUser.setKeycloakUserId("admin-789");
        currentUser.setRole("admin");

        Patient patient = new Patient();
        patient.setId(patientId);
        patient.setKeycloakUserId("patient-456");
        patient.setName("John Doe");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(null); // To be set later if needed

        User patientUser = new User();
        patientUser.setId(2L);
        patientUser.setKeycloakUserId("patient-456");
        patientUser.setRole("patient");

        Doctor doctor1 = new Doctor();
        doctor1.setId(3L);
        doctor1.setKeycloakUserId("doctor-789");
        doctor1.setName("Dr. Adams");
        doctor1.setSpecialties("Orthopedics");
        doctor1.setPrimaryCare(false);

        Doctor doctor2 = new Doctor();
        doctor2.setId(4L);
        doctor2.setKeycloakUserId("doctor-012");
        doctor2.setName("Dr. Brown");
        doctor2.setSpecialties("Pediatrics");
        doctor2.setPrimaryCare(false);

        Appointment appointment1 = new Appointment();
        appointment1.setId(5L);
        appointment1.setPatient(patient);
        appointment1.setDoctor(doctor1);
        appointment1.setAppointmentDateTime(LocalDateTime.of(2025, 5, 20, 10, 0));
        appointment1.setCreatedAt(LocalDateTime.of(2025, 5, 20, 9, 0));
        appointment1.setUpdatedAt(LocalDateTime.of(2025, 5, 20, 9, 0));

        Appointment appointment2 = new Appointment();
        appointment2.setId(6L);
        appointment2.setPatient(patient);
        appointment2.setDoctor(doctor2);
        appointment2.setAppointmentDateTime(LocalDateTime.of(2025, 5, 21, 11, 0));
        appointment2.setCreatedAt(LocalDateTime.of(2025, 5, 21, 10, 0));
        appointment2.setUpdatedAt(LocalDateTime.of(2025, 5, 21, 10, 0));

        List<Appointment> appointments = Arrays.asList(appointment1, appointment2);

        when(authenticationService.getCurrentUser()).thenReturn(currentUser);
        when(appointmentRepository.findByPatient_Id(patientId)).thenReturn(appointments);
        when(patientRepository.findById(patientId)).thenReturn(Optional.of(patient));
        when(userService.findByKeycloakUserId("patient-456")).thenReturn(patientUser);

        PatientDto patientDto = new PatientDto(
                1L,
                "John Doe",
                "patient-456",
                true,
                null, // primaryDoctorId is null
                "patient-456"
        );

        DoctorDto doctorDto1 = new DoctorDto(
                3L,
                "doctor-789",
                "Dr. Adams",
                "Orthopedics",
                false
        );

        DoctorDto doctorDto2 = new DoctorDto(
                4L,
                "doctor-012",
                "Dr. Brown",
                "Pediatrics",
                false
        );

        AppointmentDto appointmentDto1 = new AppointmentDto(
                5L,
                patientDto,
                doctorDto1,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 5, 20, 9, 0),
                LocalDateTime.of(2025, 5, 20, 9, 0),
                LocalDateTime.of(2025, 5, 20, 10, 0)
        );

        AppointmentDto appointmentDto2 = new AppointmentDto(
                6L,
                patientDto,
                doctorDto2,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 5, 21, 10, 0),
                LocalDateTime.of(2025, 5, 21, 10, 0),
                LocalDateTime.of(2025, 5, 21, 11, 0)
        );

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(appointment1, patientUser))
                    .thenReturn(appointmentDto1);
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(appointment2, patientUser))
                    .thenReturn(appointmentDto2);

            // Act
            List<AppointmentDto> result = appointmentService.findAllForPatient(patientId);

            // Assert
            assertNotNull(result);
            assertEquals(2, result.size());

            assertEquals(5L, result.get(0).getId());
            assertEquals("John Doe", result.get(0).getPatient().getName());
            assertEquals("Dr. Adams", result.get(0).getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 5, 20, 10, 0), result.get(0).getAppointmentDateTime());

            assertEquals(6L, result.get(1).getId());
            assertEquals("John Doe", result.get(1).getPatient().getName());
            assertEquals("Dr. Brown", result.get(1).getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 5, 21, 11, 0), result.get(1).getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment1, patientUser), times(1));
            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment2, patientUser), times(1));
        }

        verify(authenticationService, times(1)).getCurrentUser();
        verify(appointmentRepository, times(1)).findByPatient_Id(patientId);
        verify(patientRepository, times(1)).findById(patientId);
        verify(userService, times(1)).findByKeycloakUserId("patient-456");
    }

    /**
     * Test retrieving appointments for a doctor within a specified period.
     */
    @Test
    void testGetAppointmentsForDoctorInPeriod() {
        // Arrange
        Long doctorId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59, 59);

        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setKeycloakUserId("doctor-123");
        doctor.setName("Dr. Smith");
        doctor.setSpecialties("Cardiology");
        doctor.setPrimaryCare(true);

        Patient patient = new Patient();
        patient.setId(2L);
        patient.setKeycloakUserId("patient-456");
        patient.setName("Jane Smith");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctor(doctor);

        Appointment appointment1 = new Appointment();
        appointment1.setId(7L);
        appointment1.setPatient(patient);
        appointment1.setDoctor(doctor);
        appointment1.setAppointmentDateTime(LocalDateTime.of(2025, 1, 10, 10, 0));
        appointment1.setCreatedAt(LocalDateTime.of(2025, 1, 10, 9, 0));
        appointment1.setUpdatedAt(LocalDateTime.of(2025, 1, 10, 9, 0));

        Appointment appointment2 = new Appointment();
        appointment2.setId(8L);
        appointment2.setPatient(patient);
        appointment2.setDoctor(doctor);
        appointment2.setAppointmentDateTime(LocalDateTime.of(2025, 2, 5, 11, 0)); // Outside the period
        appointment2.setCreatedAt(LocalDateTime.of(2025, 2, 5, 10, 0));
        appointment2.setUpdatedAt(LocalDateTime.of(2025, 2, 5, 10, 0));

        List<Appointment> appointments = Arrays.asList(appointment1, appointment2);

        // Mock userService to return the patient user
        User patientUser = new User();
        patientUser.setId(2L);
        patientUser.setKeycloakUserId("patient-456");
        patientUser.setRole("patient");

        // Note: Removed authenticationService.getCurrentUser() since it's not invoked in the method under test
        when(appointmentRepository.findByDoctor_Id(doctorId)).thenReturn(appointments);
        when(userService.findByKeycloakUserId("patient-456")).thenReturn(patientUser);

        PatientDto patientDto = new PatientDto(
                2L,
                "Jane Smith",
                "patient-456",
                true,
                1L,
                "patient-456"
        );

        DoctorDto doctorDto = new DoctorDto(
                1L,
                "doctor-123",
                "Dr. Smith",
                "Cardiology",
                true
        );

        AppointmentDto appointmentDto1 = new AppointmentDto(
                7L,
                patientDto,
                doctorDto,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );

        try (MockedStatic<MappingUtils> mockedMappingUtils = mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToAppointmentDto(appointment1, patientUser))
                    .thenReturn(appointmentDto1);
            // appointment2 is outside the period, should not be mapped

            // Act
            List<AppointmentDto> result = appointmentService.getAppointmentsForDoctorInPeriod(doctorId, startDate, endDate);

            // Assert
            assertNotNull(result);
            assertEquals(1, result.size());

            assertEquals(7L, result.get(0).getId());
            assertEquals("Jane Smith", result.get(0).getPatient().getName());
            assertEquals("Dr. Smith", result.get(0).getDoctor().getName());
            assertEquals(LocalDateTime.of(2025, 1, 10, 10, 0), result.get(0).getAppointmentDateTime());

            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment1, patientUser), times(1));
            // Ensure that mapToAppointmentDto is never called for appointment2
            mockedMappingUtils.verify(() -> MappingUtils.mapToAppointmentDto(appointment2, patientUser), never());
        }

        // Removed verification for authenticationService.getCurrentUser() since it's not used
        verify(appointmentRepository, times(1)).findByDoctor_Id(doctorId);
        verify(userService, times(1)).findByKeycloakUserId("patient-456");
    }


    /**
     * Test retrieving all appointments.
     */
    @Test
    void testFindAll() {
        // Arrange
        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        appointment1.setAppointmentDateTime(LocalDateTime.of(2025, 1, 10, 10, 0));

        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        appointment2.setAppointmentDateTime(LocalDateTime.of(2025, 2, 20, 11, 0));

        List<Appointment> appointments = Arrays.asList(appointment1, appointment2);

        when(appointmentRepository.findAll()).thenReturn(appointments);

        // Act
        List<Appointment> result = appointmentService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        verify(appointmentRepository, times(1)).findAll();
    }
}
