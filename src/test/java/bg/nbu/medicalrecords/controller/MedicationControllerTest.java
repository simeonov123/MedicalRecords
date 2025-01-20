package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.Medication;
import bg.nbu.medicalrecords.service.MedicationService;
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
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MedicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MedicationService medicationService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /medications - Get all medications with valid permissions")
    @WithMockUser(authorities = {"admin", "doctor"})
    void testGetAllMedicationsWithValidPermissions() throws Exception {
        // Mocked medication list
        List<Medication> medications = Arrays.asList(
                new Medication() {{
                    setId(1L);
                    setMedicationName("Paracetamol");
                    setDosageForm("Tablet");
                    setStrength("500mg");
                    setSideEffect("Nausea");
                    setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
                    setUpdatedAt(LocalDateTime.of(2025, 1, 2, 12, 0));
                }},
                new Medication() {{
                    setId(2L);
                    setMedicationName("Ibuprofen");
                    setDosageForm("Capsule");
                    setStrength("200mg");
                    setSideEffect("Dizziness");
                    setCreatedAt(LocalDateTime.of(2025, 1, 1, 10, 0));
                    setUpdatedAt(LocalDateTime.of(2025, 1, 2, 12, 0));
                }}
        );

        // Mock service response
        Mockito.when(medicationService.getAll()).thenReturn(medications);

        // Perform GET request
        mockMvc.perform(get("/medications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].medicationName").value("Paracetamol"))
                .andExpect(jsonPath("$[0].dosageForm").value("Tablet"))
                .andExpect(jsonPath("$[0].strength").value("500mg"))
                .andExpect(jsonPath("$[0].sideEffect").value("Nausea"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].medicationName").value("Ibuprofen"))
                .andExpect(jsonPath("$[1].dosageForm").value("Capsule"))
                .andExpect(jsonPath("$[1].strength").value("200mg"))
                .andExpect(jsonPath("$[1].sideEffect").value("Dizziness"));

        // Verify service call
        Mockito.verify(medicationService, Mockito.times(1)).getAll();
    }

    @Test
    @DisplayName("GET /medications - Access denied for unauthorized users")
    @WithMockUser(authorities = {"patient"}) // Mock user with insufficient permissions
    void testGetAllMedicationsAccessDenied() throws Exception {
        mockMvc.perform(get("/medications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
    }


    @Test
    @DisplayName("GET /medications - Access denied for unauthenticated users")
    void testGetAllMedicationsUnauthenticated() throws Exception {
        mockMvc.perform(get("/medications")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
