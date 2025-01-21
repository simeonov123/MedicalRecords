package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.*;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.exception.StatisticsServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class StatisticsServiceTest {

    @Mock
    private DiagnosisService diagnosisService;

    @Mock
    private PatientService patientService;

    @Mock
    private UserService userService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private AppointmentService appointmentService;

    @Mock
    private SickLeaveService sickLeaveService;

    @InjectMocks
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        // Initialize common objects if needed
    }

    /**
     * Test successfully getting unique diagnoses.
     */
    @Test
    void getUniqueDiagnosis_Success() {
        // Arrange
        List<String> uniqueDiagnoses = Arrays.asList("Flu", "Cold", "COVID-19");
        when(diagnosisService.getUniqueDiagnosis()).thenReturn(uniqueDiagnoses);

        // Act
        List<String> result = statisticsService.getUniqueDiagnosis();

        // Assert
        assertEquals(uniqueDiagnoses, result);
        verify(diagnosisService, times(1)).getUniqueDiagnosis();
    }

    /**
     * Test failure when getting unique diagnoses.
     */
    @Test
    void getUniqueDiagnosis_Failure() {
        // Arrange
        when(diagnosisService.getUniqueDiagnosis()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getUniqueDiagnosis();
        });

        assertEquals("Failed to get unique diagnosis", exception.getMessage());
        verify(diagnosisService, times(1)).getUniqueDiagnosis();
    }

    /**
     * Test successfully getting diagnosis leaderboard.
     */
    @Test
    void getDiagnosisLeaderboard_Success() {
        // Arrange
        List<String> uniqueDiagnoses = Arrays.asList("Flu", "Cold");
        when(diagnosisService.getUniqueDiagnosis()).thenReturn(uniqueDiagnoses);

        Diagnosis diagnosis1 = new Diagnosis();
        diagnosis1.setStatement("Flu");
        diagnosis1.setDiagnosedDate(LocalDateTime.of(2023, 1, 10, 10, 0));
        Appointment appointment1 = new Appointment();
        Doctor doctor1 = new Doctor();
        doctor1.setName("Dr. Smith");
        appointment1.setDoctor(doctor1);
        diagnosis1.setAppointment(appointment1);

        Diagnosis diagnosis2 = new Diagnosis();
        diagnosis2.setStatement("Flu");
        diagnosis2.setDiagnosedDate(LocalDateTime.of(2023, 2, 15, 11, 0));
        Appointment appointment2 = new Appointment();
        Doctor doctor2 = new Doctor();
        doctor2.setName("Dr. Johnson");
        appointment2.setDoctor(doctor2);
        diagnosis2.setAppointment(appointment2);

        List<Diagnosis> fluDiagnoses = Arrays.asList(diagnosis1, diagnosis2);
        when(diagnosisService.findByStatement("Flu")).thenReturn(fluDiagnoses);
        when(diagnosisService.count()).thenReturn(2L);
        when(patientService.count()).thenReturn(1L);

        // Act
        DiagnosisStatisticsDto result = statisticsService.getDiagnosisLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.getDiagnosisDetails().size());

        DiagnosisDetailsDto details = result.getDiagnosisDetails().get(0);
        assertEquals("Flu", details.getStatement());
        assertEquals(2L, details.getCount());
        assertEquals(100L, details.getPercentageOfAllDiagnoses());
        assertEquals(100L, details.getPercentageOfAllPatients());
        assertEquals("Dr. Smith", details.getDoctorNameOfFirstDiagnosis());
        assertEquals(LocalDateTime.of(2023, 1, 10, 10, 0), details.getDateOfFirstDiagnosis());
        assertEquals(LocalDateTime.of(2023, 2, 15, 11, 0), details.getDateOfLastDiagnosis());

        verify(diagnosisService, times(1)).getUniqueDiagnosis();
        verify(diagnosisService, times(1)).findByStatement("Flu");
        verify(diagnosisService, times(2)).count();
        verify(patientService, times(2)).count();
    }

    /**
     * Test failure when getting diagnosis leaderboard.
     */
    @Test
    void getDiagnosisLeaderboard_Failure() {
        // Arrange
        when(diagnosisService.getUniqueDiagnosis()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getDiagnosisLeaderboard();
        });

        assertEquals("Failed to get diagnosis leaderboard", exception.getMessage());
        verify(diagnosisService, times(1)).getUniqueDiagnosis();
    }

//    /**
//     * Test successfully finding all patients by diagnosis statement.
//     */
//    @Test
//    void findAllByStatement_Success() {
//        // Arrange
//        String statement = "Flu";
//        Diagnosis diagnosis = new Diagnosis();
//        Appointment appointment = new Appointment();
//        Patient patient = new Patient();
//        patient.setKeycloakUserId("kc-123");
//        appointment.setPatient(patient);
//        diagnosis.setAppointment(appointment);
//
//        List<Diagnosis> diagnoses = Arrays.asList(diagnosis);
//        when(diagnosisService.findByStatement(statement)).thenReturn(diagnoses);
//
//        User user = new User();
//        user.setKeycloakUserId("kc-123");
//        user.setFirstName("John");
//        user.setLastName("Doe");
//        when(userService.findByKeycloakUserId("kc-123")).thenReturn(user);
//
//        PatientDto patientDto = new PatientDto();
//        patientDto.setId(1L);
//        patientDto.setName("John Doe");
//        patientDto.setEgn("1234567890");
//        patientDto.setHealthInsurancePaid(true);
//        patientDto.setKeycloakUserId("kc-123");
//        when(MappingUtils.mapToPatientDto(patient, user)).thenReturn(patientDto);
//
//        // Act
//        List<PatientDto> result = statisticsService.findAllByStatement(statement);
//
//        // Assert
//        assertNotNull(result);
//        assertEquals(1, result.size());
//        assertEquals("John Doe", result.get(0).getName());
//
//        verify(diagnosisService, times(1)).findByStatement(statement);
//        verify(userService, times(1)).findByKeycloakUserId("kc-123");
//        verify(MappingUtils, times(1)).mapToPatientDto(patient, user);
//    }

    /**
     * Test failure when finding all patients by diagnosis statement.
     */
    @Test
    void findAllByStatement_Failure() {
        // Arrange
        String statement = "Flu";
        when(diagnosisService.findByStatement(statement)).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.findAllByStatement(statement);
        });

        assertEquals("Failed to find all patients by diagnosis statement", exception.getMessage());
        verify(diagnosisService, times(1)).findByStatement(statement);
    }

    /**
     * Test successfully getting doctors with patient count.
     */
    @Test
    void getDoctorsWithPatientCount_Success() {
        // Arrange
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. Smith");

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Johnson");

        when(doctorService.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        Patient patient1 = new Patient();
        patient1.setId(1L);
        patient1.setPrimaryDoctor(doctor1);

        Patient patient2 = new Patient();
        patient2.setId(2L);
        patient2.setPrimaryDoctor(doctor1);

        when(patientService.findAllByPrimaryDoctorId(1L)).thenReturn(Arrays.asList(new PatientDto(), new PatientDto()));
        when(patientService.findAllByPrimaryDoctorId(2L)).thenReturn(Collections.singletonList(new PatientDto()));

        // Act
        List<DoctorPatientCountDto> result = statisticsService.getDoctorsWithPatientCount();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        DoctorPatientCountDto dto1 = result.get(0);
        assertEquals("Dr. Smith", dto1.getDoctorName());
        assertEquals(2L, dto1.getCount());

        DoctorPatientCountDto dto2 = result.get(1);
        assertEquals("Dr. Johnson", dto2.getDoctorName());
        assertEquals(1L, dto2.getCount());

        verify(doctorService, times(1)).findAll();
        verify(patientService, times(1)).findAllByPrimaryDoctorId(1L);
        verify(patientService, times(1)).findAllByPrimaryDoctorId(2L);
    }

    /**
     * Test failure when getting doctors with patient count.
     */
    @Test
    void getDoctorsWithPatientCount_Failure() {
        // Arrange
        when(doctorService.findAll()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getDoctorsWithPatientCount();
        });

        assertEquals("Failed to get doctors with patient count", exception.getMessage());
        verify(doctorService, times(1)).findAll();
    }

    /**
     * Test successfully getting doctors with appointments count.
     */
    @Test
    void getDoctorsWithAppointmentsCount_Success() {
        // Arrange
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. Smith");

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Johnson");

        when(doctorService.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        Appointment appointment1 = new Appointment();
        appointment1.setId(1L);
        Appointment appointment2 = new Appointment();
        appointment2.setId(2L);
        Appointment appointment3 = new Appointment();
        appointment3.setId(3L);

        when(appointmentService.findAllByDoctorId(1L)).thenReturn(Arrays.asList(appointment1, appointment2));
        when(appointmentService.findAllByDoctorId(2L)).thenReturn(Collections.singletonList(appointment3));

        // Act
        List<DoctorAppointmentsCount> result = statisticsService.getDoctorsWithAppointmentsCount();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        DoctorAppointmentsCount dto1 = result.get(0);
        assertEquals("Dr. Smith", dto1.getDoctorName());
        assertEquals(2L, dto1.getCount());

        DoctorAppointmentsCount dto2 = result.get(1);
        assertEquals("Dr. Johnson", dto2.getDoctorName());
        assertEquals(1L, dto2.getCount());

        verify(doctorService, times(1)).findAll();
        verify(appointmentService, times(1)).findAllByDoctorId(1L);
        verify(appointmentService, times(1)).findAllByDoctorId(2L);
    }

    /**
     * Test failure when getting doctors with appointments count.
     */
    @Test
    void getDoctorsWithAppointmentsCount_Failure() {
        // Arrange
        when(doctorService.findAll()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getDoctorsWithAppointmentsCount();
        });

        assertEquals("Failed to get doctors with appointments count", exception.getMessage());
        verify(doctorService, times(1)).findAll();
    }

    /**
     * Test successfully getting doctors with appointments in period.
     */
    @Test
    void getDoctorsWithAppointmentsInPeriod_Success() {
        // Arrange
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. Smith");

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Johnson");

        when(doctorService.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        Appointment appointment1 = new Appointment();
        appointment1.setDoctor(doctor1);
        appointment1.setAppointmentDateTime(LocalDateTime.of(2023, 5, 10, 10, 0));

        Appointment appointment2 = new Appointment();
        appointment2.setDoctor(doctor1);
        appointment2.setAppointmentDateTime(LocalDateTime.of(2023, 5, 15, 11, 0));

        Appointment appointment3 = new Appointment();
        appointment3.setDoctor(doctor2);
        appointment3.setAppointmentDateTime(LocalDateTime.of(2023, 6, 20, 12, 0));

        when(appointmentService.findAllByDoctorId(1L)).thenReturn(Arrays.asList(appointment1, appointment2));
        when(appointmentService.findAllByDoctorId(2L)).thenReturn(Collections.singletonList(appointment3));

        LocalDateTime startDate = LocalDateTime.of(2023, 5, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 5, 31, 23, 59);

        // Act
        List<DoctorsThatHaveAppointmentsInPeriod> result = statisticsService.getDoctorsWithAppointmentsInPeriod(startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());

        DoctorsThatHaveAppointmentsInPeriod dto = result.get(0);
        assertEquals("Dr. Smith", dto.getDoctorName());
        assertEquals(1L, dto.getDoctorId());
        assertEquals(LocalDate.of(2023, 5, 1), dto.getStartDate());
        assertEquals(LocalDate.of(2023, 5, 31), dto.getEndDate());

        verify(doctorService, times(1)).findAll();
        verify(appointmentService, times(1)).findAllByDoctorId(1L);
        verify(appointmentService, times(1)).findAllByDoctorId(2L);
    }

    /**
     * Test failure when getting doctors with appointments in period.
     */
    @Test
    void getDoctorsWithAppointmentsInPeriod_Failure() {
        // Arrange
        when(doctorService.findAll()).thenThrow(new RuntimeException("Service failure"));

        LocalDateTime startDate = LocalDateTime.of(2023, 5, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2023, 5, 31, 23, 59);

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getDoctorsWithAppointmentsInPeriod(startDate, endDate);
        });

        assertEquals("Failed to get doctors with appointments in period", exception.getMessage());
        verify(doctorService, times(1)).findAll();
    }



    /**
     * Test failure when getting most sick leaves month data.
     */
    @Test
    void getMostSickLeavesMonthData_Failure() {
        // Arrange
        when(sickLeaveService.findAllSickLeaves()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getMostSickLeavesMonthData();
        });

        assertEquals("Failed to get most sick leaves month data", exception.getMessage());
        verify(sickLeaveService, times(1)).findAllSickLeaves();
    }

    /**
     * Test successfully getting doctors sick leaves leaderboard.
     */
    @Test
    void getDoctorsSickLeavesLeaderboard_Success() {
        // Arrange
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. Smith");
        doctor1.setSpecialties("Cardiology");
        doctor1.setPrimaryCare(true);

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Johnson");
        doctor2.setSpecialties("Neurology");
        doctor2.setPrimaryCare(false);

        when(doctorService.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        Appointment appointment1 = new Appointment();
        appointment1.setDoctor(doctor1);
        appointment1.setSickLeaves(Arrays.asList(new SickLeave(), new SickLeave()));

        Appointment appointment2 = new Appointment();
        appointment2.setDoctor(doctor1);
        appointment2.setSickLeaves(Collections.singletonList(new SickLeave()));

        Appointment appointment3 = new Appointment();
        appointment3.setDoctor(doctor2);
        appointment3.setSickLeaves(Collections.singletonList(new SickLeave()));

        when(appointmentService.findAll()).thenReturn(Arrays.asList(appointment1, appointment2, appointment3));

        // Act
        List<DoctorsSickLeavesLeaderboardDto> result = statisticsService.getDoctorsSickLeavesLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        DoctorsSickLeavesLeaderboardDto dto1 = result.get(0);
        assertEquals("Dr. Smith", dto1.getName());
        assertEquals("Cardiology", dto1.getSpecialties());
        assertTrue(dto1.isPrimaryCare());
        assertEquals(3, dto1.getSickLeavesCount());

        DoctorsSickLeavesLeaderboardDto dto2 = result.get(1);
        assertEquals("Dr. Johnson", dto2.getName());
        assertEquals("Neurology", dto2.getSpecialties());
        assertFalse(dto2.isPrimaryCare());
        assertEquals(1, dto2.getSickLeavesCount());

        verify(doctorService, times(1)).findAll();
        verify(appointmentService, times(1)).findAll();
    }

    /**
     * Test failure when getting doctors sick leaves leaderboard.
     */
    @Test
    void getDoctorsSickLeavesLeaderboard_Failure() {
        // Arrange
        when(doctorService.findAll()).thenThrow(new RuntimeException("Service failure"));

        // Act & Assert
        StatisticsServiceException exception = assertThrows(StatisticsServiceException.class, () -> {
            statisticsService.getDoctorsSickLeavesLeaderboard();
        });

        assertEquals("Failed to get doctors sick leaves leaderboard", exception.getMessage());
        verify(doctorService, times(1)).findAll();
    }

    /**
     * Test successfully getting doctors sick leaves leaderboard with no sick leaves.
     */
    @Test
    void getDoctorsSickLeavesLeaderboard_NoSickLeaves() {
        // Arrange
        Doctor doctor1 = new Doctor();
        doctor1.setId(1L);
        doctor1.setName("Dr. Smith");
        doctor1.setSpecialties("Cardiology");
        doctor1.setPrimaryCare(true);

        Doctor doctor2 = new Doctor();
        doctor2.setId(2L);
        doctor2.setName("Dr. Johnson");
        doctor2.setSpecialties("Neurology");
        doctor2.setPrimaryCare(false);

        when(doctorService.findAll()).thenReturn(Arrays.asList(doctor1, doctor2));

        Appointment appointment1 = new Appointment();
        appointment1.setDoctor(doctor1);
        appointment1.setSickLeaves(Collections.emptyList());

        Appointment appointment2 = new Appointment();
        appointment2.setDoctor(doctor2);
        appointment2.setSickLeaves(Collections.emptyList());

        when(appointmentService.findAll()).thenReturn(Arrays.asList(appointment1, appointment2));

        // Act
        List<DoctorsSickLeavesLeaderboardDto> result = statisticsService.getDoctorsSickLeavesLeaderboard();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());

        DoctorsSickLeavesLeaderboardDto dto1 = result.get(0);
        assertEquals("Dr. Smith", dto1.getName());
        assertEquals("Cardiology", dto1.getSpecialties());
        assertTrue(dto1.isPrimaryCare());
        assertEquals(0, dto1.getSickLeavesCount());

        DoctorsSickLeavesLeaderboardDto dto2 = result.get(1);
        assertEquals("Dr. Johnson", dto2.getName());
        assertEquals("Neurology", dto2.getSpecialties());
        assertFalse(dto2.isPrimaryCare());
        assertEquals(0, dto2.getSickLeavesCount());

        verify(doctorService, times(1)).findAll();
        verify(appointmentService, times(1)).findAll();
    }
}
