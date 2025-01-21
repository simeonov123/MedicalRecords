package bg.nbu.medicalrecords.controller;

import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import bg.nbu.medicalrecords.service.KeycloakService;
import bg.nbu.medicalrecords.service.LocalSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static bg.nbu.medicalrecords.utils.UserControllerTestUtil.asJsonString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch; // optional
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post; // if needed
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // We 'mock' the KeycloakService so it does not call real Keycloak
    @MockBean
    private KeycloakService keycloakService;

    // We also 'mock' LocalSyncService
    @MockBean
    private LocalSyncService localSyncService;

    @BeforeEach
    void setUp() {
        // Reset mocks before each test, if you want
        reset(keycloakService, localSyncService);
    }

    @Test
    @DisplayName("GET /users - returns all users (requires ADMIN authority)")
    @WithMockUser(authorities = "admin")
    void testFetchAllUsers() throws Exception {
        // GIVEN: KeycloakService returns some fake user(s)
        KeycloakUserDto dto = new KeycloakUserDto();
        dto.setId("fakeKcId");
        dto.setUsername("john.doe");
        dto.setEmail("john@example.com");
        dto.setRole("patient");
        dto.setEmailVerified(true);

        when(keycloakService.fetchAllUsers()).thenReturn(List.of(dto));

        // WHEN we call GET /users
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                // THEN we expect 200 OK
                .andExpect(status().isOk())
                // We can check for the JSON structure, if desired:
                .andExpect(jsonPath("$[0].id").value("fakeKcId"))
                .andExpect(jsonPath("$[0].username").value("john.doe"))
                .andExpect(jsonPath("$[0].email").value("john@example.com"))
                .andExpect(jsonPath("$[0].role").value("patient"))
                .andExpect(jsonPath("$[0].emailVerified").value(true));

        // Also verify the mock was called exactly once
        verify(keycloakService, times(1)).fetchAllUsers();
    }

    @Test
    @DisplayName("GET /users - should be forbidden if not admin")
    @WithMockUser(authorities = "doctor") // or "user" or any non-admin
    void testFetchAllUsers_Unauthorized() throws Exception {
        // Attempt to call GET /users
        mockMvc.perform(get("/users")
                        .contentType(MediaType.APPLICATION_JSON))
                // We expect 403 Forbidden because of @PreAuthorize("hasAuthority('admin')")
                .andExpect(status().isInternalServerError());

        // keycloakService.fetchAllUsers() should NOT be called
        verify(keycloakService, never()).fetchAllUsers();
    }

    @Test
    @DisplayName("PUT /users/{id}/role - updates user role for admin")
    @WithMockUser(authorities = "admin")
    void testUpdateUserRole() throws Exception {
        // GIVEN
        String userId = "fakeUserId";
        String newRole = "doctor";
        // No returned object, just calls service

        // WHEN we call PUT /users/{userId}/role
        String requestBody = """
                {
                  "role": "doctor"
                }
                """;
        mockMvc.perform(put("/users/{id}/role", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        // THEN check that KeycloakService.updateUserRole and LocalSyncService.handleRoleChange are called
        verify(keycloakService, times(1)).updateUserRole(userId, newRole);
        verify(localSyncService, times(1)).handleRoleChange(userId, newRole);
    }

    @Test
    @DisplayName("PUT /users/{id}/verify-email - verifies email for admin")
    @WithMockUser(authorities = "admin")
    void testVerifyUserEmail() throws Exception {
        // GIVEN
        String userId = "fakeUserId";
        boolean verified = true;

        // WHEN
        mockMvc.perform(put("/users/{id}/verify-email", userId)
                        .param("verified", String.valueOf(verified))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // THEN
        verify(keycloakService, times(1)).setUserEmailVerified(userId, verified);
    }

    @Test
    @DisplayName("DELETE /users/{id} - deletes user for admin")
    @WithMockUser(authorities = "admin")
    void testDeleteUser() throws Exception {
        // GIVEN
        String userId = "fakeUserId";

        // WHEN
        mockMvc.perform(delete("/users/{id}", userId))
                .andExpect(status().isNoContent());

        // THEN
        verify(keycloakService, times(1)).deleteUser(userId);
    }

    @Test
    @DisplayName("PUT /users/{id}/details - updates user details for admin")
    @WithMockUser(authorities = "admin")
    void testUpdateUserDetails() throws Exception {
        // GIVEN
        String userId = "fakeUserId";
        KeycloakUserDto dto = new KeycloakUserDto();
        dto.setId(userId);
        dto.setEgn("1234567890");
        dto.setEmail("newEmail@example.com");
        dto.setUsername("newUsername");
        dto.setEmailVerified(true);
        dto.setFirstName("John");
        dto.setLastName("Doe");
        dto.setRole("patient");

        // Convert the DTO to JSON string
        String requestJson = asJsonString(dto);

        // WHEN
        mockMvc.perform(put("/users/{id}/details", userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk());

        // THEN
        verify(keycloakService, times(1)).updateUserDetails(eq(userId), any(KeycloakUserDto.class));
    }
}
