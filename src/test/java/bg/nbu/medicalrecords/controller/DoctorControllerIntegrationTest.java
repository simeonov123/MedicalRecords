package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Doctor;
import bg.nbu.medicalrecords.service.DoctorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DoctorService doctorService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reset(doctorService);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("GET /doctors - Find all doctors")
    @WithMockUser(authorities = {"admin", "doctor", "patient"})
    void testFindAll() throws Exception {
        // GIVEN
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. John Doe");
        when(doctorService.findAll()).thenReturn(Collections.singletonList(doctor));

        // WHEN
        mockMvc.perform(get("/doctors")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].name").value("Dr. John Doe"));

        verify(doctorService, times(1)).findAll();
    }

    @Test
    @DisplayName("POST /doctors - Create a doctor")
    @WithMockUser(authorities = "admin")
    void testCreateDoctor() throws Exception {
        // GIVEN
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. John Doe");

        when(doctorService.createDoctor(any(Doctor.class))).thenReturn(doctor);

        // WHEN
        mockMvc.perform(post("/doctors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doctor)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dr. John Doe"));

        verify(doctorService, times(1)).createDoctor(any(Doctor.class));
    }

    @Test
    @DisplayName("PUT /doctors/{keycloakUserId} - Update a doctor by Keycloak User ID")
    @WithMockUser(authorities = "admin")
    void testUpdateByKeycloakUserId() throws Exception {
        // GIVEN
        Doctor updatedDoctor = new Doctor();
        updatedDoctor.setId(1L);
        updatedDoctor.setName("Dr. Jane Doe");

        when(doctorService.updateDoctorByKeycloakUserId(anyString(), any(Doctor.class))).thenReturn(updatedDoctor);

        // WHEN
        mockMvc.perform(put("/doctors/keycloak123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedDoctor)))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dr. Jane Doe"));

        verify(doctorService, times(1)).updateDoctorByKeycloakUserId(eq("keycloak123"), any(Doctor.class));
    }

    @Test
    @DisplayName("DELETE /doctors/{id} - Delete a doctor")
    @WithMockUser(authorities = "admin")
    void testDeleteDoctor() throws Exception {
        // WHEN
        mockMvc.perform(delete("/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isNoContent());

        verify(doctorService, times(1)).deleteDoctor(1L);
    }

    @Test
    @DisplayName("GET /doctors/{id} - Find doctor by ID")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindById() throws Exception {
        // GIVEN
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. John Doe");

        when(doctorService.findById(1L)).thenReturn(doctor);

        // WHEN
        mockMvc.perform(get("/doctors/1")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dr. John Doe"));

        verify(doctorService, times(1)).findById(1L);
    }

    @Test
    @DisplayName("GET /doctors/doctor - Find doctor by principal")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testFindByPrincipal() throws Exception {
        // GIVEN
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setName("Dr. John Doe");

        when(doctorService.findByPrincipal()).thenReturn(doctor);

        // WHEN
        mockMvc.perform(get("/doctors/doctor")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.name").value("Dr. John Doe"));

        verify(doctorService, times(1)).findByPrincipal();
    }
}
