package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Diagnosis;
import bg.nbu.medicalrecords.domain.Medication;
import bg.nbu.medicalrecords.domain.Prescription;
import bg.nbu.medicalrecords.domain.SickLeave;
import bg.nbu.medicalrecords.dto.*;
import bg.nbu.medicalrecords.service.*;
import bg.nbu.medicalrecords.util.MappingUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the AppointmentController.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class AppointmentControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private SickLeaveService sickLeaveService;

    @MockBean
    private DiagnosisService diagnosisService;

    @MockBean
    private TreatmentService treatmentService;

    @MockBean
    private PrescriptionService prescriptionService;

    @MockBean
    private MappingUtils mappingUtils;


    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /appointments/getAppointmentsForLoggedInUser - Get all appointments for logged in user")
    @WithMockUser(authorities = {"patient", "admin", "doctor"})
    void testFindAllForLoggedInUser() throws Exception {
        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setEgn("1234567890");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctorId(1L);

        DoctorDto doctor = new DoctorDto();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setSpecialties(Arrays.asList("Cardiology", "Internal Medicine").toString());

        DiagnosisDto diagnosis1 = new DiagnosisDto(
                1L,
                "Hypertension",
                LocalDateTime.of(2025, 1, 15, 10, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                Collections.emptyList()
        );

        SickLeaveDto sickLeave1 = new SickLeaveDto(
                1L,
                "Recovery from surgery",
                LocalDate.of(2025, 1, 16),
                LocalDate.of(2025, 1, 16),
                LocalDate.of(2025, 1, 25),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0)
        );

        AppointmentDto appointment1 = new AppointmentDto(
                1L,
                patient,
                doctor,
                Collections.singletonList(diagnosis1),
                Collections.singletonList(sickLeave1),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 9, 0),
                LocalDateTime.of(2025, 1, 15, 10, 0)
        );

        List<AppointmentDto> appointments = Arrays.asList(appointment1);

        Mockito.when(appointmentService.findAllForLoggedInUser()).thenReturn(appointments);

        mockMvc.perform(get("/appointments/getAppointmentsForLoggedInUser"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].patient.id").value(1))
                .andExpect(jsonPath("$[0].patient.name").value("John Doe"))
                .andExpect(jsonPath("$[0].doctor.id").value(1))
                .andExpect(jsonPath("$[0].doctor.name").value("Dr. Smith"))
                .andExpect(jsonPath("$[0].diagnoses.length()").value(1))
                .andExpect(jsonPath("$[0].diagnoses[0].id").value(1))
                .andExpect(jsonPath("$[0].sickLeaves.length()").value(1))
                .andExpect(jsonPath("$[0].sickLeaves[0].id").value(1))
                .andExpect(jsonPath("$[0].appointmentDateTime").value("2025-01-15T10:00:00"));

        Mockito.verify(appointmentService, Mockito.times(1)).findAllForLoggedInUser();
    }

    @Test
    @DisplayName("GET /appointments/{patientId}/appointments - Get all appointments for a patient")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindAllForPatient() throws Exception {
        Long patientId = 1L;

        PatientDto patient = new PatientDto();
        patient.setId(patientId);
        patient.setName("Jane Doe");
        patient.setEgn("0987654321");
        patient.setHealthInsurancePaid(false);
        patient.setPrimaryDoctorId(2L);

        DoctorDto doctor = new DoctorDto();
        doctor.setId(2L);
        doctor.setName("Dr. Johnson");
        doctor.setSpecialties(Collections.singletonList("Neurology").toString());
        doctor.setPrimaryCare(false);

        DiagnosisDto diagnosis1 = new DiagnosisDto(
                2L,
                "Diabetes",
                LocalDateTime.of(2025, 2, 10, 11, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                Collections.emptyList()
        );

        SickLeaveDto sickLeave1 = new SickLeaveDto(
                2L,
                "Insulin therapy",
                LocalDate.of(2025, 2, 11),
                LocalDate.of(2025, 2, 11),
                LocalDate.of(2025, 2, 20),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0)
        );

        AppointmentDto appointment1 = new AppointmentDto(
                2L,
                patient,
                doctor,
                Collections.singletonList(diagnosis1),
                Collections.singletonList(sickLeave1),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                LocalDateTime.of(2025, 2, 10, 10, 0),
                LocalDateTime.of(2025, 2, 10, 11, 0)
        );

        List<AppointmentDto> appointments = Arrays.asList(appointment1);

        Mockito.when(appointmentService.findAllForPatient(patientId)).thenReturn(appointments);

        mockMvc.perform(get("/appointments/{patientId}/appointments", patientId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(2))
                .andExpect(jsonPath("$[0].patient.id").value(patientId))
                .andExpect(jsonPath("$[0].patient.name").value("Jane Doe"))
                .andExpect(jsonPath("$[0].doctor.id").value(2))
                .andExpect(jsonPath("$[0].doctor.name").value("Dr. Johnson"))
                .andExpect(jsonPath("$[0].diagnoses.length()").value(1))
                .andExpect(jsonPath("$[0].diagnoses[0].id").value(2))
                .andExpect(jsonPath("$[0].sickLeaves.length()").value(1))
                .andExpect(jsonPath("$[0].sickLeaves[0].id").value(2))
                .andExpect(jsonPath("$[0].appointmentDateTime").value("2025-02-10T11:00:00"));

        Mockito.verify(appointmentService, Mockito.times(1)).findAllForPatient(patientId);
    }

    @Test
    @DisplayName("POST /appointments - Create a new appointment")
    @WithMockUser(authorities = {"patient", "admin", "doctor"})
    void testCreateAppointment() throws Exception {
        CreateAppointmentDto createDto = new CreateAppointmentDto();
        createDto.setPatientId(1L);
        createDto.setDoctorId(1L);
        createDto.setDate(LocalDateTime.of(2025, 3, 15, 10, 0));

        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setEgn("1234567890");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctorId(1L);

        DoctorDto doctor = new DoctorDto();
        doctor.setId(1L);
        doctor.setName("Dr. Smith");
        doctor.setSpecialties(Arrays.asList("Cardiology", "Internal Medicine").toString());
        doctor.setPrimaryCare(true);
        AppointmentDto createdAppointment = new AppointmentDto(
                3L,
                patient,
                doctor,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 3, 15, 9, 0),
                LocalDateTime.of(2025, 3, 15, 9, 0),
                LocalDateTime.of(2025, 3, 15, 10, 0)
        );

        Mockito.when(appointmentService.createAppointment(any(CreateAppointmentDto.class))).thenReturn(createdAppointment);

        mockMvc.perform(post("/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(3))
                .andExpect(jsonPath("$.patient.id").value(1))
                .andExpect(jsonPath("$.patient.name").value("John Doe"))
                .andExpect(jsonPath("$.doctor.id").value(1))
                .andExpect(jsonPath("$.doctor.name").value("Dr. Smith"))
                .andExpect(jsonPath("$.appointmentDateTime").value("2025-03-15T10:00:00"));

        Mockito.verify(appointmentService, Mockito.times(1)).createAppointment(any(CreateAppointmentDto.class));
    }

    @Test
    @DisplayName("PUT /appointments/{id} - Update an appointment")
    @WithMockUser(authorities = {"patient", "admin", "doctor"})
    void testUpdateAppointment() throws Exception {
        Long appointmentId = 1L;
        UpdateAppointmentDto updateDto = new UpdateAppointmentDto(
                2L,
                LocalDateTime.of(2025, 4, 10, 15, 0)
        );

        PatientDto patient = new PatientDto();
        patient.setId(1L);
        patient.setName("John Doe");
        patient.setEgn("1234567890");
        patient.setHealthInsurancePaid(true);
        patient.setPrimaryDoctorId(1L);

        DoctorDto updatedDoctor = new DoctorDto();
        updatedDoctor.setId(2L);
        updatedDoctor.setName("Dr. Johnson");
        updatedDoctor.setSpecialties(Collections.singletonList("Neurology").toString());
        updatedDoctor.setPrimaryCare(false);

        AppointmentDto updatedAppointment = new AppointmentDto(
                appointmentId,
                patient,
                updatedDoctor,
                Collections.emptyList(),
                Collections.emptyList(),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 14, 0),
                LocalDateTime.of(2025, 4, 10, 15, 0)
        );

        Mockito.when(appointmentService.updateAppointment(eq(appointmentId), any(UpdateAppointmentDto.class)))
                .thenReturn(updatedAppointment);

        mockMvc.perform(put("/appointments/{id}", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(appointmentId))
                .andExpect(jsonPath("$.doctor.id").value(2))
                .andExpect(jsonPath("$.doctor.name").value("Dr. Johnson"))
                .andExpect(jsonPath("$.appointmentDateTime").value("2025-04-10T15:00:00"));

        Mockito.verify(appointmentService, Mockito.times(1)).updateAppointment(eq(appointmentId), any(UpdateAppointmentDto.class));
    }

    @Test
    @DisplayName("DELETE /appointments/{id} - Delete an appointment")
    @WithMockUser(authorities = {"patient", "admin", "doctor"})
    void testDeleteAppointment() throws Exception {
        Long appointmentId = 1L;

        Mockito.doNothing().when(appointmentService).deleteAppointment(appointmentId);

        mockMvc.perform(delete("/appointments/{id}", appointmentId))
                .andExpect(status().isNoContent());

        Mockito.verify(appointmentService, Mockito.times(1)).deleteAppointment(appointmentId);
    }

    @Test
    @DisplayName("POST /appointments/{appointmentId}/sick-leave - Create a sick leave")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testCreateSickLeave() throws Exception {
        Long appointmentId = 1L;

        // Input DTO for the request
        SickLeaveDto sickLeaveDto = new SickLeaveDto(
                1L,
                "Recovery from surgery",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 10),
                LocalDateTime.of(2025, 5, 1, 9, 0),
                LocalDateTime.of(2025, 5, 1, 9, 0)
        );

        // Mock SickLeave entity returned by the service
        SickLeave sickLeave = new SickLeave();
        sickLeave.setId(1L);
        sickLeave.setReason("Recovery from surgery");
        sickLeave.setTodayDate(LocalDate.of(2025, 5, 1));
        sickLeave.setStartDate(LocalDate.of(2025, 5, 1));
        sickLeave.setEndDate(LocalDate.of(2025, 5, 10));
        sickLeave.setCreatedAt(LocalDateTime.of(2025, 5, 1, 9, 0));
        sickLeave.setUpdatedAt(LocalDateTime.of(2025, 5, 1, 9, 0));

        // Mock the service call
        Mockito.when(sickLeaveService.createSickLeave(eq(appointmentId), any(SickLeaveDto.class)))
                .thenReturn(sickLeave);

        // Mock static MappingUtils behavior
        try (MockedStatic<MappingUtils> mockedMappingUtils = Mockito.mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToSickLeaveDto(sickLeave))
                    .thenReturn(sickLeaveDto);

            // Perform request and assertions
            mockMvc.perform(post("/appointments/{appointmentId}/sick-leave", appointmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(sickLeaveDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.reason").value("Recovery from surgery"))
                    .andExpect(jsonPath("$.todayDate").value("2025-05-01"))
                    .andExpect(jsonPath("$.startDate").value("2025-05-01"))
                    .andExpect(jsonPath("$.endDate").value("2025-05-10"))
                    .andExpect(jsonPath("$.createdAt").value("2025-05-01T09:00:00"))
                    .andExpect(jsonPath("$.updatedAt").value("2025-05-01T09:00:00"));

            // Verify interactions
            Mockito.verify(sickLeaveService, Mockito.times(1))
                    .createSickLeave(eq(appointmentId), any(SickLeaveDto.class));
            mockedMappingUtils.verify(() -> MappingUtils.mapToSickLeaveDto(sickLeave), Mockito.times(1));
        }
    }


    @Test
    @DisplayName("PUT /appointments/{appointmentId}/sick-leave/{sickLeaveId} - Update a sick leave")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testUpdateSickLeave() throws Exception {
        Long appointmentId = 1L;
        Long sickLeaveId = 1L;

        // Mock input DTO
        UpdateSickLeaveDto updateSickLeaveDto = new UpdateSickLeaveDto();
        updateSickLeaveDto.setEndDate(LocalDate.of(2025, 5, 12));
        updateSickLeaveDto.setReason("Extended recovery");

        // Mock returned SickLeave entity
        SickLeave sickLeave = new SickLeave();
        sickLeave.setId(sickLeaveId);
        sickLeave.setReason("Extended recovery");
        sickLeave.setTodayDate(LocalDate.of(2025, 5, 1));
        sickLeave.setStartDate(LocalDate.of(2025, 5, 1));
        sickLeave.setEndDate(LocalDate.of(2025, 5, 12));
        sickLeave.setCreatedAt(LocalDateTime.of(2025, 5, 1, 9, 0));
        sickLeave.setUpdatedAt(LocalDateTime.of(2025, 5, 1, 9, 0));

        // Mock returned DTO
        SickLeaveDto sickLeaveDto = new SickLeaveDto(
                sickLeaveId,
                "Extended recovery",
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 12),
                LocalDateTime.of(2025, 5, 1, 9, 0),
                LocalDateTime.of(2025, 5, 1, 9, 0)
        );

        // Mock the service call
        Mockito.when(sickLeaveService.updateSickLeave(eq(appointmentId), any(UpdateSickLeaveDto.class), eq(sickLeaveId)))
                .thenReturn(sickLeave);

        // Mock MappingUtils behavior using a mocked static method
        try (MockedStatic<MappingUtils> mockedMappingUtils = Mockito.mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToSickLeaveDto(sickLeave))
                    .thenReturn(sickLeaveDto);

            // Perform request
            mockMvc.perform(put("/appointments/{appointmentId}/sick-leave/{sickLeaveId}", appointmentId, sickLeaveId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateSickLeaveDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(sickLeaveId))
                    .andExpect(jsonPath("$.reason").value("Extended recovery"))
                    .andExpect(jsonPath("$.endDate").value("2025-05-12"));

            // Verify static method interaction
            mockedMappingUtils.verify(() -> MappingUtils.mapToSickLeaveDto(sickLeave), Mockito.times(1));
        }

        // Verify service interaction
        Mockito.verify(sickLeaveService, Mockito.times(1))
                .updateSickLeave(eq(appointmentId), any(UpdateSickLeaveDto.class), eq(sickLeaveId));
    }





    @Test
    @DisplayName("DELETE /appointments/{appointmentId}/sick-leave/{sickLeaveId} - Delete a sick leave")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testDeleteSickLeave() throws Exception {
        Long appointmentId = 1L;
        Long sickLeaveId = 1L;

        Mockito.doNothing().when(sickLeaveService).deleteSickLeave(sickLeaveId, appointmentId);

        mockMvc.perform(delete("/appointments/{appointmentId}/sick-leave/{sickLeaveId}", appointmentId, sickLeaveId))
                .andExpect(status().isNoContent());

        Mockito.verify(sickLeaveService, Mockito.times(1)).deleteSickLeave(sickLeaveId, appointmentId);
    }

    @Test
    @DisplayName("POST /appointments/{appointmentId}/diagnosis - Create a diagnosis")
    @WithMockUser(authorities = {"doctor"})
    void testCreateDiagnosis() throws Exception {
        Long appointmentId = 1L;
        CreateDiagnosisDto createDiagnosisDto = new CreateDiagnosisDto();
        createDiagnosisDto.setStatement("Hypertension");
        createDiagnosisDto.setDiagnosedDate(LocalDateTime.of(2025, 6, 1, 10, 0));

        Diagnosis diagnosis = new Diagnosis();
        diagnosis.setId(1L);
        diagnosis.setStatement("Hypertension");
        diagnosis.setDiagnosedDate(LocalDateTime.of(2025, 6, 1, 10, 0));
        diagnosis.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));
        diagnosis.setUpdatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));

        Mockito.when(diagnosisService.createDiagnosis(eq(appointmentId), any(CreateDiagnosisDto.class)))
                .thenReturn(diagnosis);

        mockMvc.perform(post("/appointments/{appointmentId}/diagnosis", appointmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDiagnosisDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.statement").value("Hypertension"))
                .andExpect(jsonPath("$.diagnosedDate").value("2025-06-01T10:00:00"));

        Mockito.verify(diagnosisService, Mockito.times(1))
                .createDiagnosis(eq(appointmentId), any(CreateDiagnosisDto.class));
    }

    @Test
    @DisplayName("PUT /appointments/{appointmentId}/diagnosis/{diagnosisId} - Update a diagnosis")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testUpdateDiagnosis() throws Exception {
        Long appointmentId = 1L;
        Long diagnosisId = 1L;

        // Instantiate UpdateDiagnosisDto using the all-args constructor
        UpdateDiagnosisDto updateDiagnosisDto = new UpdateDiagnosisDto(
                "Updated Hypertension",
                LocalDateTime.of(2025, 6, 2, 11, 0)
        );

        // Create the updated Diagnosis domain object
        Diagnosis updatedDiagnosis = new Diagnosis();
        updatedDiagnosis.setId(diagnosisId);
        updatedDiagnosis.setStatement("Updated Hypertension");
        updatedDiagnosis.setDiagnosedDate(LocalDateTime.of(2025, 6, 2, 11, 0));
        updatedDiagnosis.setCreatedAt(LocalDateTime.of(2025, 6, 1, 10, 0));
        updatedDiagnosis.setUpdatedAt(LocalDateTime.of(2025, 6, 2, 11, 0));

        // Create the expected DiagnosisDto
        DiagnosisDto diagnosisDto = new DiagnosisDto(
                diagnosisId,
                "Updated Hypertension",
                LocalDateTime.of(2025, 6, 2, 11, 0),
                LocalDateTime.of(2025, 6, 1, 10, 0),
                LocalDateTime.of(2025, 6, 2, 11, 0),
                Collections.emptyList()
        );

        // Mock the diagnosisService.updateDiagnosis method
        Mockito.when(diagnosisService.updateDiagnosis(eq(appointmentId), eq(diagnosisId), any(UpdateDiagnosisDto.class)))
                .thenReturn(updatedDiagnosis);

        // Mock the static MappingUtils.mapToDiagnosisDto method
        try (MockedStatic<MappingUtils> mockedMappingUtils = Mockito.mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToDiagnosisDto(updatedDiagnosis))
                    .thenReturn(diagnosisDto);

            // Perform the PUT request
            mockMvc.perform(put("/appointments/{appointmentId}/diagnosis/{diagnosisId}", appointmentId, diagnosisId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDiagnosisDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(diagnosisId))
                    .andExpect(jsonPath("$.statement").value("Updated Hypertension"))
                    .andExpect(jsonPath("$.diagnosedDate").value("2025-06-02T11:00:00"));

            // Verify that the static method was called once
            ((MockedStatic<?>) mockedMappingUtils).verify(() -> MappingUtils.mapToDiagnosisDto(updatedDiagnosis), Mockito.times(1));
        }

        // Verify that diagnosisService.updateDiagnosis was called once
        Mockito.verify(diagnosisService, Mockito.times(1))
                .updateDiagnosis(eq(appointmentId), eq(diagnosisId), any(UpdateDiagnosisDto.class));
    }

    @Test
    @DisplayName("DELETE /appointments/{appointmentId}/diagnosis/{diagnosisId} - Delete a diagnosis")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testDeleteDiagnosis() throws Exception {
        Long appointmentId = 1L;
        Long diagnosisId = 1L;

        Mockito.doNothing().when(diagnosisService).deleteDiagnosis(diagnosisId, appointmentId);

        mockMvc.perform(delete("/appointments/{appointmentId}/diagnosis/{diagnosisId}", appointmentId, diagnosisId))
                .andExpect(status().isNoContent());

        Mockito.verify(diagnosisService, Mockito.times(1)).deleteDiagnosis(diagnosisId, appointmentId);
    }

    @Test
    @DisplayName("POST /appointments/{appointmentId}/diagnosis/{diagnosisId}/treatment - Create a treatment")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testCreateTreatment() throws Exception {
        Long appointmentId = 1L;
        Long diagnosisId = 1L;

        CreateTreatmentDto createTreatmentDto = new CreateTreatmentDto(
                LocalDate.of(2025, 6, 3),
                LocalDate.of(2025, 6, 10),
                "Physical therapy sessions"
        );

        TreatmentDto treatmentDto = new TreatmentDto(
                1L,
                LocalDateTime.of(2025, 6, 3, 9, 0),
                LocalDateTime.of(2025, 6, 3, 9, 0),
                LocalDate.of(2025, 6, 3),
                LocalDate.of(2025, 6, 10),
                Collections.emptyList(),
                "Physical therapy sessions"
        );

        Mockito.when(treatmentService.createTreatment(eq(appointmentId), eq(diagnosisId), any(CreateTreatmentDto.class)))
                .thenReturn(treatmentDto);

        mockMvc.perform(post("/appointments/{appointmentId}/diagnosis/{diagnosisId}/treatment", appointmentId, diagnosisId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createTreatmentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Physical therapy sessions"))
                .andExpect(jsonPath("$.startDate").value("2025-06-03"))
                .andExpect(jsonPath("$.endDate").value("2025-06-10"));

        Mockito.verify(treatmentService, Mockito.times(1))
                .createTreatment(eq(appointmentId), eq(diagnosisId), any(CreateTreatmentDto.class));
    }

    @Test
    @DisplayName("POST /appointments/{appointmentId}/treatments/{treatmentId}/prescriptions - Create a prescription")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testCreatePrescription() throws Exception {
        Long appointmentId = 1L;
        Long treatmentId = 1L;

        // Mock input DTO
        CreatePrescriptionDto createPrescriptionDto = new CreatePrescriptionDto(
                1L,
                "5mg",
                30
        );

        // Mock Medication entity
        Medication medication = new Medication();
        medication.setId(1L);
        medication.setMedicationName("Medication A");
        medication.setDosageForm("Description A");
        medication.setStrength("Side effects A");
        medication.setSideEffect("Contraindications A");
        medication.setCreatedAt(LocalDateTime.of(2025, 6, 4, 10, 0));
        medication.setUpdatedAt(LocalDateTime.of(2025, 6, 4, 10, 0));

        // Mock Prescription entity
        Prescription prescription = new Prescription();
        prescription.setId(1L);
        prescription.setMedication(medication); // Ensure medication is set
        prescription.setDosage("5mg");
        prescription.setDuration(30);
        prescription.setCreatedAt(LocalDateTime.of(2025, 6, 4, 10, 0));
        prescription.setUpdatedAt(LocalDateTime.of(2025, 6, 4, 10, 0));

        // Mock MedicationDto
        MedicationDto medicationDto = new MedicationDto(
                1L,
                "Medication A",
                "Description A",
                "Side effects A",
                "Contraindications A",
                LocalDateTime.of(2025, 6, 4, 10, 0),
                LocalDateTime.of(2025, 6, 4, 10, 0)
        );

        // Mock PrescriptionDto
        PrescriptionDto prescriptionDto = new PrescriptionDto(
                1L,
                medicationDto,
                "5mg",
                30,
                LocalDateTime.of(2025, 6, 4, 10, 0),
                LocalDateTime.of(2025, 6, 4, 10, 0)
        );

        // Mock service and static method
        Mockito.when(prescriptionService.createPrescription(eq(appointmentId), eq(treatmentId), any(CreatePrescriptionDto.class)))
                .thenReturn(prescription);

        try (MockedStatic<MappingUtils> mockedMappingUtils = Mockito.mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToPrescriptionDto(prescription))
                    .thenReturn(prescriptionDto);

            // Perform request
            mockMvc.perform(post("/appointments/{appointmentId}/treatments/{treatmentId}/prescriptions", appointmentId, treatmentId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(createPrescriptionDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.medication.id").value(1))
                    .andExpect(jsonPath("$.medication.medicationName").value("Medication A"))
                    .andExpect(jsonPath("$.dosage").value("5mg"))
                    .andExpect(jsonPath("$.duration").value(30));

            // Verify service interaction
            Mockito.verify(prescriptionService, Mockito.times(1))
                    .createPrescription(eq(appointmentId), eq(treatmentId), any(CreatePrescriptionDto.class));

            mockedMappingUtils.verify(() -> MappingUtils.mapToPrescriptionDto(prescription), Mockito.times(1));
        }
    }



    @Test
    @DisplayName("PUT /appointments/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId} - Update a prescription")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testUpdatePrescription() throws Exception {
        Long appointmentId = 1L;
        Long treatmentId = 1L;
        Long prescriptionId = 1L;

        UpdatePrescriptionDto updatePrescriptionDto = new UpdatePrescriptionDto(
                2L,
                "10mg",
                60
        );

        Prescription updatedPrescription = new Prescription();
        updatedPrescription.setId(prescriptionId);
        updatedPrescription.setDosage("10mg");
        updatedPrescription.setDuration(60);
        updatedPrescription.setCreatedAt(LocalDateTime.of(2025, 6, 4, 10, 0));
        updatedPrescription.setUpdatedAt(LocalDateTime.of(2025, 6, 5, 11, 0));

        Medication medication = new Medication();
        medication.setId(2L);
        medication.setMedicationName("Medication B");
        medication.setDosageForm("Description B");
        medication.setStrength("Side effects B");
        medication.setCreatedAt(LocalDateTime.of(2025, 6, 5, 11, 0));
        medication.setUpdatedAt(LocalDateTime.of(2025, 6, 5, 11, 0));

        updatedPrescription.setMedication(medication);

        PrescriptionDto prescriptionDto = new PrescriptionDto(
                prescriptionId,
                new MedicationDto(
                        2L,
                        "Medication B",
                        "Description B",
                        "Side effects B",
                        "Contraindications B",
                        LocalDateTime.of(2025, 6, 5, 11, 0),
                        LocalDateTime.of(2025, 6, 5, 11, 0)
                ),
                "10mg",
                60,
                LocalDateTime.of(2025, 6, 4, 10, 0),
                LocalDateTime.of(2025, 6, 5, 11, 0)
        );

        Mockito.when(prescriptionService.updatePrescription(eq(appointmentId), eq(treatmentId), eq(prescriptionId), any(UpdatePrescriptionDto.class)))
                .thenReturn(updatedPrescription);

        try (MockedStatic<MappingUtils> mockedMappingUtils = Mockito.mockStatic(MappingUtils.class)) {
            mockedMappingUtils.when(() -> MappingUtils.mapToPrescriptionDto(updatedPrescription))
                    .thenReturn(prescriptionDto);

            mockMvc.perform(put("/appointments/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId}", appointmentId, treatmentId, prescriptionId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updatePrescriptionDto)))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                    .andExpect(jsonPath("$.id").value(prescriptionId))
                    .andExpect(jsonPath("$.medication.id").value(2))
                    .andExpect(jsonPath("$.medication.medicationName").value("Medication B")) // Updated path
                    .andExpect(jsonPath("$.dosage").value("10mg"))
                    .andExpect(jsonPath("$.duration").value(60));

            mockedMappingUtils.verify(() -> MappingUtils.mapToPrescriptionDto(updatedPrescription), Mockito.times(1));
        }

        Mockito.verify(prescriptionService, Mockito.times(1))
                .updatePrescription(eq(appointmentId), eq(treatmentId), eq(prescriptionId), any(UpdatePrescriptionDto.class));
    }



    @Test
    @DisplayName("DELETE /appointments/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId} - Delete a prescription")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testDeletePrescription() throws Exception {
        Long appointmentId = 1L;
        Long treatmentId = 1L;
        Long prescriptionId = 1L;

        Mockito.doNothing().when(prescriptionService).deletePrescription(prescriptionId, appointmentId, treatmentId);

        mockMvc.perform(delete("/appointments/{appointmentId}/treatments/{treatmentId}/prescriptions/{prescriptionId}", appointmentId, treatmentId, prescriptionId))
                .andExpect(status().isNoContent());

        Mockito.verify(prescriptionService, Mockito.times(1))
                .deletePrescription(prescriptionId, appointmentId, treatmentId);
    }

    @Test
    @DisplayName("PUT /appointments/{appointmentId}/treatments/{treatmentId} - Update a treatment")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testUpdateTreatment() throws Exception {
        Long appointmentId = 1L;
        Long treatmentId = 1L;

        UpdateTreatmentDto updateTreatmentDto = new UpdateTreatmentDto(
                LocalDate.of(2025, 6, 4),
                LocalDate.of(2025, 6, 15),
                "Updated physical therapy sessions"
        );

        TreatmentDto updatedTreatmentDto = new TreatmentDto(
                treatmentId,
                LocalDateTime.of(2025, 6, 4, 9, 0),
                LocalDateTime.of(2025, 6, 4, 9, 0),
                LocalDate.of(2025, 6, 4),
                LocalDate.of(2025, 6, 15),
                Collections.emptyList(),
                "Updated physical therapy sessions"
        );

        Mockito.when(treatmentService.updateTreatment(eq(appointmentId), eq(treatmentId), any(UpdateTreatmentDto.class)))
                .thenReturn(updatedTreatmentDto);

        mockMvc.perform(put("/appointments/{appointmentId}/treatments/{treatmentId}", appointmentId, treatmentId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateTreatmentDto)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(treatmentId))
                .andExpect(jsonPath("$.description").value("Updated physical therapy sessions"))
                .andExpect(jsonPath("$.startDate").value("2025-06-04"))
                .andExpect(jsonPath("$.endDate").value("2025-06-15"));

        Mockito.verify(treatmentService, Mockito.times(1))
                .updateTreatment(eq(appointmentId), eq(treatmentId), any(UpdateTreatmentDto.class));
    }

    @Test
    @DisplayName("DELETE /appointments/{appointmentId}/treatments/{treatmentId} - Delete a treatment")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testDeleteTreatment() throws Exception {
        Long appointmentId = 1L;
        Long treatmentId = 1L;

        Mockito.doNothing().when(treatmentService).deleteTreatment(appointmentId, treatmentId);

        mockMvc.perform(delete("/appointments/{appointmentId}/treatments/{treatmentId}", appointmentId, treatmentId))
                .andExpect(status().isNoContent());

        Mockito.verify(treatmentService, Mockito.times(1)).deleteTreatment(appointmentId, treatmentId);
    }

    @Test
    @DisplayName("GET /appointments/doctor/{doctorId} - Get appointments for a doctor in a period")
    @WithMockUser(authorities = {"doctor", "admin"})
    void testGetAppointmentsForDoctorInPeriod() throws Exception {
        Long doctorId = 1L;
        LocalDateTime startDate = LocalDateTime.of(2025, 1, 1, 0, 0);
        LocalDateTime endDate = LocalDateTime.of(2025, 1, 31, 23, 59, 59);

        PatientDto patient = new PatientDto();
        patient.setId(2L);
        patient.setName("Jane Smith");
        patient.setPrimaryDoctorId(2L);

        DoctorDto doctor = new DoctorDto();
        doctor.setId(doctorId);
        doctor.setName("Dr. Johnson");
        doctor.setSpecialties(Collections.singletonList("Neurology").toString());

        DiagnosisDto diagnosis1 = new DiagnosisDto(
                3L,
                "Migraine",
                LocalDateTime.of(2025, 1, 10, 10, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                Collections.emptyList()
        );

        SickLeaveDto sickLeave1 = new SickLeaveDto(
                3L,
                "Rest and recovery",
                LocalDate.of(2025, 1, 11),
                LocalDate.of(2025, 1, 11),
                LocalDate.of(2025, 1, 20),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0)
        );

        AppointmentDto appointment1 = new AppointmentDto(
                3L,
                patient,
                doctor,
                Collections.singletonList(diagnosis1),
                Collections.singletonList(sickLeave1),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 9, 0),
                LocalDateTime.of(2025, 1, 10, 10, 0)
        );

        List<AppointmentDto> appointments = Arrays.asList(appointment1);

        Mockito.when(appointmentService.getAppointmentsForDoctorInPeriod(eq(doctorId), eq(startDate), eq(endDate)))
                .thenReturn(appointments);

        mockMvc.perform(get("/appointments/doctor/{doctorId}", doctorId)
                        .param("startDate", "2025-01-01T00:00:00")
                        .param("endDate", "2025-01-31T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(3))
                .andExpect(jsonPath("$[0].patient.id").value(2))
                .andExpect(jsonPath("$[0].patient.name").value("Jane Smith"))
                .andExpect(jsonPath("$[0].doctor.id").value(1))
                .andExpect(jsonPath("$[0].doctor.name").value("Dr. Johnson"))
                .andExpect(jsonPath("$[0].diagnoses.length()").value(1))
                .andExpect(jsonPath("$[0].diagnoses[0].id").value(3))
                .andExpect(jsonPath("$[0].sickLeaves.length()").value(1))
                .andExpect(jsonPath("$[0].sickLeaves[0].id").value(3))
                .andExpect(jsonPath("$[0].appointmentDateTime").value("2025-01-10T10:00:00"));

        Mockito.verify(appointmentService, Mockito.times(1))
                .getAppointmentsForDoctorInPeriod(eq(doctorId), eq(startDate), eq(endDate));
    }
}
