package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.StatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Annotation to load the full application context
@SpringBootTest
// Annotation to auto-configure MockMvc
@AutoConfigureMockMvc
class StatisticsControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StatisticsService statisticsService;

    @MockBean
    private PatientService patientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /statistics/diagnoses/unique - Get unique diagnoses")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetUniqueDiagnosis() throws Exception {
        List<String> uniqueDiagnoses = Arrays.asList("Flu", "Cold", "COVID-19");

        Mockito.when(statisticsService.getUniqueDiagnosis()).thenReturn(uniqueDiagnoses);

        mockMvc.perform(get("/statistics/diagnoses/unique"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(uniqueDiagnoses.size()))
                .andExpect(jsonPath("$[0]").value("Flu"))
                .andExpect(jsonPath("$[1]").value("Cold"))
                .andExpect(jsonPath("$[2]").value("COVID-19"));

        Mockito.verify(statisticsService, Mockito.times(1)).getUniqueDiagnosis();
    }

    @Test
    @DisplayName("GET /statistics/diagnoses/leaderboard - Get diagnosis leaderboard")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetDiagnosisLeaderboard() throws Exception {
        DiagnosisDetailsDto fluDetails = new DiagnosisDetailsDto();
        fluDetails.setStatement("Flu");
        fluDetails.setCount(50L);
        fluDetails.setPercentageOfAllDiagnoses(25L);
        fluDetails.setPercentageOfAllPatients(10L);
        fluDetails.setDoctorNameOfFirstDiagnosis("Dr. Smith");
        fluDetails.setDateOfFirstDiagnosis(LocalDateTime.of(2025, 1, 1, 10, 0));
        fluDetails.setDateOfLastDiagnosis(LocalDateTime.of(2025, 1, 31, 18, 0));

        DiagnosisDetailsDto covidDetails = new DiagnosisDetailsDto();
        covidDetails.setStatement("COVID-19");
        covidDetails.setCount(100L);
        covidDetails.setPercentageOfAllDiagnoses(50L);
        covidDetails.setPercentageOfAllPatients(20L);
        covidDetails.setDoctorNameOfFirstDiagnosis("Dr. Johnson");
        covidDetails.setDateOfFirstDiagnosis(LocalDateTime.of(2025, 1, 2, 9, 30));
        covidDetails.setDateOfLastDiagnosis(LocalDateTime.of(2025, 1, 30, 17, 45));

        DiagnosisStatisticsDto leaderboard = new DiagnosisStatisticsDto();
        leaderboard.setDiagnosisDetails(Arrays.asList(covidDetails, fluDetails));

        Mockito.when(statisticsService.getDiagnosisLeaderboard()).thenReturn(leaderboard);

        mockMvc.perform(get("/statistics/diagnoses/leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.diagnosisDetails.length()").value(2))
                .andExpect(jsonPath("$.diagnosisDetails[0].statement").value("COVID-19"))
                .andExpect(jsonPath("$.diagnosisDetails[0].count").value(100))
                .andExpect(jsonPath("$.diagnosisDetails[0].percentageOfAllDiagnoses").value(50))
                .andExpect(jsonPath("$.diagnosisDetails[0].percentageOfAllPatients").value(20))
                .andExpect(jsonPath("$.diagnosisDetails[0].doctorNameOfFirstDiagnosis").value("Dr. Johnson"))
                .andExpect(jsonPath("$.diagnosisDetails[0].dateOfFirstDiagnosis").value("2025-01-02T09:30:00"))
                .andExpect(jsonPath("$.diagnosisDetails[0].dateOfLastDiagnosis").value("2025-01-30T17:45:00"))
                .andExpect(jsonPath("$.diagnosisDetails[1].statement").value("Flu"))
                .andExpect(jsonPath("$.diagnosisDetails[1].count").value(50))
                .andExpect(jsonPath("$.diagnosisDetails[1].percentageOfAllDiagnoses").value(25))
                .andExpect(jsonPath("$.diagnosisDetails[1].percentageOfAllPatients").value(10))
                .andExpect(jsonPath("$.diagnosisDetails[1].doctorNameOfFirstDiagnosis").value("Dr. Smith"))
                .andExpect(jsonPath("$.diagnosisDetails[1].dateOfFirstDiagnosis").value("2025-01-01T10:00:00"))
                .andExpect(jsonPath("$.diagnosisDetails[1].dateOfLastDiagnosis").value("2025-01-31T18:00:00"));

        Mockito.verify(statisticsService, Mockito.times(1)).getDiagnosisLeaderboard();
    }

    @Test
    @DisplayName("GET /statistics/patients/byDoctor/{doctorId} - Get patients by primary doctor")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetPatientsByPrimaryDoctor() throws Exception {
        Long doctorId = 1L;
        PatientDto patient1 = new PatientDto();
        patient1.setId(1L);
        patient1.setName("John Doe");
        patient1.setEgn("1234567890");
        patient1.setHealthInsurancePaid(true);
        patient1.setPrimaryDoctorId(doctorId);

        PatientDto patient2 = new PatientDto();
        patient2.setId(2L);
        patient2.setName("Jane Smith");
        patient2.setEgn("0987654321");
        patient2.setHealthInsurancePaid(false);
        patient2.setPrimaryDoctorId(doctorId);

        List<PatientDto> patients = Arrays.asList(patient1, patient2);

        Mockito.when(patientService.findAllByPrimaryDoctorId(doctorId)).thenReturn(patients);

        mockMvc.perform(get("/statistics/patients/byDoctor/{doctorId}", doctorId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(patient1.getId()))
                .andExpect(jsonPath("$[0].name").value(patient1.getName()))
                .andExpect(jsonPath("$[0].egn").value(patient1.getEgn()))
                .andExpect(jsonPath("$[0].primaryDoctorId").value(doctorId))
                .andExpect(jsonPath("$[1].id").value(patient2.getId()))
                .andExpect(jsonPath("$[1].name").value(patient2.getName()))
                .andExpect(jsonPath("$[1].egn").value(patient2.getEgn()))
                .andExpect(jsonPath("$[1].primaryDoctorId").value(doctorId));

        Mockito.verify(patientService, Mockito.times(1)).findAllByPrimaryDoctorId(doctorId);
    }

    @Test
    @DisplayName("GET /statistics/doctors-with-patient-count - Get doctors with patient count")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetDoctorsWithPatientCount() throws Exception {
        DoctorPatientCountDto doctor1 = new DoctorPatientCountDto();
        doctor1.setDoctorName("Dr. Alice");
        doctor1.setCount(10L);

        DoctorPatientCountDto doctor2 = new DoctorPatientCountDto();
        doctor2.setDoctorName("Dr. Bob");
        doctor2.setCount(15L);

        List<DoctorPatientCountDto> doctorsWithPatientCount = Arrays.asList(doctor1, doctor2);

        Mockito.when(statisticsService.getDoctorsWithPatientCount()).thenReturn(doctorsWithPatientCount);

        mockMvc.perform(get("/statistics/doctors-with-patient-count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Alice"))
                .andExpect(jsonPath("$[0].count").value(10))
                .andExpect(jsonPath("$[1].doctorName").value("Dr. Bob"))
                .andExpect(jsonPath("$[1].count").value(15));

        Mockito.verify(statisticsService, Mockito.times(1)).getDoctorsWithPatientCount();
    }

    @Test
    @DisplayName("GET /statistics/doctors-with-appointments-count - Get doctors with appointments count")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetDoctorsWithAppointmentsCount() throws Exception {
        DoctorAppointmentsCount doctor1 = new DoctorAppointmentsCount();
        doctor1.setDoctorName("Dr. Alice");
        doctor1.setCount(100L);

        DoctorAppointmentsCount doctor2 = new DoctorAppointmentsCount();
        doctor2.setDoctorName("Dr. Bob");
        doctor2.setCount(150L);

        List<DoctorAppointmentsCount> doctorsWithAppointmentsCount = Arrays.asList(doctor1, doctor2);

        Mockito.when(statisticsService.getDoctorsWithAppointmentsCount()).thenReturn(doctorsWithAppointmentsCount);

        mockMvc.perform(get("/statistics/doctors-with-appointments-count"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Alice"))
                .andExpect(jsonPath("$[0].count").value(100))
                .andExpect(jsonPath("$[1].doctorName").value("Dr. Bob"))
                .andExpect(jsonPath("$[1].count").value(150));

        Mockito.verify(statisticsService, Mockito.times(1)).getDoctorsWithAppointmentsCount();
    }

    @Test
    @DisplayName("GET /statistics/doctors-with-appointments-in-period - Get doctors with appointments in period")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetDoctorsWithAppointmentsInPeriod() throws Exception {
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 5, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 29, 0, 0);

        DoctorsThatHaveAppointmentsInPeriod doctorPeriod1 = new DoctorsThatHaveAppointmentsInPeriod();
        doctorPeriod1.setDoctorName("Dr. Alice");
        doctorPeriod1.setDoctorId(1L);
        doctorPeriod1.setStartDate(startDate.toLocalDate());
        doctorPeriod1.setEndDate(endDate.toLocalDate());

        DoctorsThatHaveAppointmentsInPeriod doctorPeriod2 = new DoctorsThatHaveAppointmentsInPeriod();
        doctorPeriod2.setDoctorName("Dr. Bob");
        doctorPeriod2.setDoctorId(2L);
        doctorPeriod2.setStartDate(startDate.toLocalDate());
        doctorPeriod2.setEndDate(endDate.toLocalDate());

        List<DoctorsThatHaveAppointmentsInPeriod> doctorsInPeriod = Arrays.asList(doctorPeriod1, doctorPeriod2);

        Mockito.when(statisticsService.getDoctorsWithAppointmentsInPeriod(eq(startDate), eq(endDate)))
                .thenReturn(doctorsInPeriod);

        mockMvc.perform(get("/statistics/doctors-with-appointments-in-period")
                        .param("startDate", "2025-01-05T00:00:00")
                        .param("endDate", "2025-01-29T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].doctorName").value("Dr. Alice"))
                .andExpect(jsonPath("$[0].doctorId").value(1))
                .andExpect(jsonPath("$[0].startDate").value("2025-01-05"))
                .andExpect(jsonPath("$[0].endDate").value("2025-01-29"))
                .andExpect(jsonPath("$[1].doctorName").value("Dr. Bob"))
                .andExpect(jsonPath("$[1].doctorId").value(2))
                .andExpect(jsonPath("$[1].startDate").value("2025-01-05"))
                .andExpect(jsonPath("$[1].endDate").value("2025-01-29"));

        Mockito.verify(statisticsService, Mockito.times(1))
                .getDoctorsWithAppointmentsInPeriod(eq(startDate), eq(endDate));
    }

    @Test
    @DisplayName("GET /statistics/most-sick-leaves-month-data - Get most sick leaves month data")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetMostSickLeavesMonthData() throws Exception {
        MostSickLeavesMonthData sickLeavesData = new MostSickLeavesMonthData();
        sickLeavesData.setMonthName("January");
        sickLeavesData.setSickLeavesCount(20);
        sickLeavesData.setAppointmentsThatMonthCount(200);
        sickLeavesData.setUniquePatientsCount(150);
        sickLeavesData.setMostCommonDiagnosisThatMonth("Flu");

        Mockito.when(statisticsService.getMostSickLeavesMonthData()).thenReturn(sickLeavesData);

        mockMvc.perform(get("/statistics/most-sick-leaves-month-data"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.monthName").value("January"))
                .andExpect(jsonPath("$.sickLeavesCount").value(20))
                .andExpect(jsonPath("$.appointmentsThatMonthCount").value(200))
                .andExpect(jsonPath("$.uniquePatientsCount").value(150))
                .andExpect(jsonPath("$.mostCommonDiagnosisThatMonth").value("Flu"));

        Mockito.verify(statisticsService, Mockito.times(1)).getMostSickLeavesMonthData();
    }

    @Test
    @DisplayName("GET /statistics/doctors-sick-leaves-leaderboard - Get doctors sick leaves leaderboard")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetDoctorsSickLeavesLeaderboard() throws Exception {
        // Create first doctor DTO with specialties as a String
        DoctorsSickLeavesLeaderboardDto doctor1 = new DoctorsSickLeavesLeaderboardDto();
        doctor1.setName("Dr. Alice");
        doctor1.setSpecialties("[Cardiology, Internal Medicine]"); // Set as String
        doctor1.setPrimaryCare(true);
        doctor1.setSickLeavesCount(5);

        // Create second doctor DTO with specialties as a String
        DoctorsSickLeavesLeaderboardDto doctor2 = new DoctorsSickLeavesLeaderboardDto();
        doctor2.setName("Dr. Bob");
        doctor2.setSpecialties("[Neurology]"); // Set as String
        doctor2.setPrimaryCare(false);
        doctor2.setSickLeavesCount(3);

        // Mock the service response
        List<DoctorsSickLeavesLeaderboardDto> leaderboard = Arrays.asList(doctor1, doctor2);
        Mockito.when(statisticsService.getDoctorsSickLeavesLeaderboard()).thenReturn(leaderboard);

        // Perform the GET request and assert the response
        mockMvc.perform(get("/statistics/doctors-sick-leaves-leaderboard"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2)) // Expecting two items in the list

                // Assertions for the first doctor
                .andExpect(jsonPath("$[0].name").value("Dr. Alice"))
                .andExpect(jsonPath("$[0].specialties").value("[Cardiology, Internal Medicine]")) // Expecting the exact String
                .andExpect(jsonPath("$[0].primaryCare").value(true))
                .andExpect(jsonPath("$[0].sickLeavesCount").value(5))

                // Assertions for the second doctor
                .andExpect(jsonPath("$[1].name").value("Dr. Bob"))
                .andExpect(jsonPath("$[1].specialties").value("[Neurology]")) // Expecting the exact String
                .andExpect(jsonPath("$[1].primaryCare").value(false))
                .andExpect(jsonPath("$[1].sickLeavesCount").value(3));

        // Verify that the service method was called once
        Mockito.verify(statisticsService, Mockito.times(1)).getDoctorsSickLeavesLeaderboard();
    }

}
