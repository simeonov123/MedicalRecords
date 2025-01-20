package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.CreatePatientDto;
import bg.nbu.medicalrecords.dto.PatientDto;
import bg.nbu.medicalrecords.dto.UpdatePatientDto;
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

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PatientControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PatientService patientService;

    @MockBean
    private StatisticsService statisticsService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("POST /patients - Create patient")
    @WithMockUser(authorities = "admin")
    void testCreatePatient() throws Exception {
        CreatePatientDto createDto = new CreatePatientDto();
        createDto.setName("John Doe");
        createDto.setEgn("1234567890");
        createDto.setHealthInsurancePaid(true); // Set to a non-null value
        createDto.setPrimaryDoctorId(null); // Optional field

        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");
        patientDto.setEgn("1234567890");
        patientDto.setHealthInsurancePaid(true);
        patientDto.setPrimaryDoctorId(null);

        Mockito.when(patientService.createPatient(any(CreatePatientDto.class))).thenReturn(patientDto);

        mockMvc.perform(post("/patients")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientDto.getId()))
                .andExpect(jsonPath("$.name").value(patientDto.getName()))
                .andExpect(jsonPath("$.egn").value(patientDto.getEgn()))
                .andExpect(jsonPath("$.primaryDoctorId").doesNotExist());

        Mockito.verify(patientService, Mockito.times(1)).createPatient(any(CreatePatientDto.class));
    }


    @Test
    @DisplayName("PUT /patients/{id} - Update patient")
    @WithMockUser(authorities = "admin")
    void testUpdatePatient() throws Exception {
        UpdatePatientDto updateDto = new UpdatePatientDto();
        updateDto.setName("Jane Doe");
        updateDto.setHealthInsurancePaid(false); // Set to a non-null value
        updateDto.setEgn(null); // Optional field
        updateDto.setPrimaryDoctorId(null); // Optional field

        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("Jane Doe");
        patientDto.setHealthInsurancePaid(false);
        patientDto.setEgn(null);
        patientDto.setPrimaryDoctorId(null);

        Mockito.when(patientService.updatePatient(eq("1"), any(UpdatePatientDto.class))).thenReturn(patientDto);

        mockMvc.perform(put("/patients/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientDto.getId()))
                .andExpect(jsonPath("$.name").value(patientDto.getName()))
                .andExpect(jsonPath("$.egn").doesNotExist())
                .andExpect(jsonPath("$.primaryDoctorId").doesNotExist());

        Mockito.verify(patientService, Mockito.times(1)).updatePatient(eq("1"), any(UpdatePatientDto.class));
    }


    @Test
    @DisplayName("DELETE /patients/{id} - Delete patient")
    @WithMockUser(authorities = "admin")
    void testDeletePatient() throws Exception {
        mockMvc.perform(delete("/patients/1"))
                .andExpect(status().isNoContent());

        Mockito.verify(patientService, Mockito.times(1)).deletePatient(1L);
    }

    @Test
    @DisplayName("GET /patients - Find all patients")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindAllPatients() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");

        Mockito.when(patientService.findAll()).thenReturn(List.of(patientDto));

        mockMvc.perform(get("/patients"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(patientDto.getId()))
                .andExpect(jsonPath("$[0].name").value(patientDto.getName()));

        Mockito.verify(patientService, Mockito.times(1)).findAll();
    }

    @Test
    @DisplayName("GET /patients/searchByDiagnosis - Find patients by diagnosis")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindAllByStatement() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");

        Mockito.when(statisticsService.findAllByStatement(eq("Flu"))).thenReturn(List.of(patientDto));

        mockMvc.perform(get("/patients/searchByDiagnosis?diagnosis=Flu"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(patientDto.getId()))
                .andExpect(jsonPath("$[0].name").value(patientDto.getName()));

        Mockito.verify(statisticsService, Mockito.times(1)).findAllByStatement(eq("Flu"));
    }

    @Test
    @DisplayName("GET /patients/{id} - Find patient by ID")
    @WithMockUser(authorities = {"admin", "doctor", "patient"})
    void testFindPatientById() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");

        Mockito.when(patientService.findById(1L)).thenReturn(patientDto);

        mockMvc.perform(get("/patients/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientDto.getId()))
                .andExpect(jsonPath("$.name").value(patientDto.getName()));

        Mockito.verify(patientService, Mockito.times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /patients/egn/{egn} - Find patient by EGN")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindPatientByEgn() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");

        Mockito.when(patientService.findByEgn("1234567890")).thenReturn(patientDto);

        mockMvc.perform(get("/patients/egn/1234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientDto.getId()))
                .andExpect(jsonPath("$.name").value(patientDto.getName()));

        Mockito.verify(patientService, Mockito.times(1)).findByEgn("1234567890");
    }

    @Test
    @DisplayName("PUT /patients/{id}/primary-doctor/{doctorId} - Assign primary doctor")
    @WithMockUser(authorities = "admin")
    void testAssignPrimaryDoctor() throws Exception {
        mockMvc.perform(put("/patients/1/primary-doctor/2"))
                .andExpect(status().isOk());

        Mockito.verify(patientService, Mockito.times(1)).assignPrimaryDoctor(1L, 2L);
    }

    @Test
    @DisplayName("PUT /patients/{id}/health-insurance - Update health insurance status")
    @WithMockUser(authorities = "admin")
    void testUpdateHealthInsuranceStatus() throws Exception {
        mockMvc.perform(put("/patients/1/health-insurance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"healthInsurancePaid\":true}"))
                .andExpect(status().isOk());

        Mockito.verify(patientService, Mockito.times(1)).updateHealthInsuranceStatus(1L, true);
    }

    @Test
    @DisplayName("GET /patients/keycloak-user-id/{keycloakUserId} - Find patient by Keycloak User ID")
    @WithMockUser(authorities = {"admin", "doctor", "patient"})
    void testFindByKeycloakUserId() throws Exception {
        PatientDto patientDto = new PatientDto();
        patientDto.setId(1L);
        patientDto.setName("John Doe");

        Mockito.when(patientService.findByKeycloakUserId("kcUserId")).thenReturn(patientDto);

        mockMvc.perform(get("/patients/keycloak-user-id/kcUserId"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(patientDto.getId()))
                .andExpect(jsonPath("$.name").value(patientDto.getName()));

        Mockito.verify(patientService, Mockito.times(1)).findByKeycloakUserId("kcUserId");
    }
}
