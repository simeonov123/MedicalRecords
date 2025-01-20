package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.RegistrationDto;
import bg.nbu.medicalrecords.service.KeycloakService;
import bg.nbu.medicalrecords.service.PatientService;
import bg.nbu.medicalrecords.service.UserService;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private UserService userService;

    @MockBean
    private PatientService patientService;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Reset all mocks before each test
        reset(keycloakService, userService, patientService);
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("POST /auth/signup - Register user successfully")
    void testSignup() throws Exception {
        // GIVEN
        String userId = "mockKeycloakUserId";
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setUsername("john.doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setPassword("Test@123");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEgn("1234567890");
        registrationDto.setDesiredRole("patient");

        User savedUser = new User();
        savedUser.setKeycloakUserId(userId);
        savedUser.setUsername("john.doe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setFirstName("John");
        savedUser.setLastName("Doe");
        savedUser.setEgn("1234567890");
        savedUser.setRole("patient");

        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenReturn(userId);
        when(userService.createUser(any(User.class))).thenReturn(savedUser);

        // Argument Captors
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        // WHEN
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                // THEN
                .andExpect(status().isOk());

        // Verify that KeycloakService and UserService were called
        verify(keycloakService, times(1)).createUser(
                eq("john.doe"),
                eq("john.doe@example.com"),
                eq("Test@123"),
                eq("John"),
                eq("Doe")
        );
        verify(keycloakService, times(1)).assignRole(eq(userId), eq("patient"));
        verify(userService, times(2)).createUser(userCaptor.capture());
        verify(patientService, times(1)).createPatientFromKeycloak(eq(userId), eq("john.doe"));

        // Assertions on captured arguments
        List<User> capturedUsers = userCaptor.getAllValues();
        assertThat(capturedUsers).hasSize(2);

        User firstCallUser = capturedUsers.get(0);
        assertThat(firstCallUser.getUsername()).isEqualTo("john.doe");
        assertThat(firstCallUser.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(firstCallUser.getEgn()).isEqualTo("1234567890");
        assertThat(firstCallUser.getRole()).isNull(); // Role not set yet

        User secondCallUser = capturedUsers.get(1);
        assertThat(secondCallUser.getRole()).isEqualTo("patient");
    }

    @Test
    @DisplayName("POST /auth/signup - Fails when KeycloakService throws exception")
    void testSignupFails() throws Exception {
        // GIVEN
        RegistrationDto registrationDto = new RegistrationDto();
        registrationDto.setUsername("john.doe");
        registrationDto.setEmail("john.doe@example.com");
        registrationDto.setPassword("Test@123");
        registrationDto.setFirstName("John");
        registrationDto.setLastName("Doe");
        registrationDto.setEgn("1234567890");
        registrationDto.setDesiredRole("patient");

        when(keycloakService.createUser(anyString(), anyString(), anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Keycloak error"));

        // WHEN
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registrationDto)))
                // THEN
                .andExpect(status().isInternalServerError());

        // Verify that UserService and PatientService were NOT called
        verify(userService, never()).createUser(any());
        verify(patientService, never()).createPatientFromKeycloak(anyString(), anyString());
    }

    @Test
    @DisplayName("POST /auth/sync - Trigger Keycloak user sync")
    @WithMockUser(authorities = "admin") // Add this annotation to simulate an authenticated admin user
    void testSyncUsers() throws Exception {
        // WHEN
        mockMvc.perform(post("/auth/sync")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN
                .andExpect(status().isOk());

        // Verify that KeycloakService.syncUsers() was called
        verify(keycloakService, times(1)).syncUsers();
    }
}
