package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.dto.KeycloakUserDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.*;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class KeycloakServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private UserService userService;

    @Mock
    private DoctorService doctorService;

    @Mock
    private PatientService patientService;

    @Spy
    @InjectMocks
    private KeycloakService keycloakService;

    // Constants for Token Retrieval
    private final String tokenUrl = "http://localhost:8080/realms/medical-realm/protocol/openid-connect/token";
    private final String tokenBody = "grant_type=password"
            + "&client_id=medical-backend"
            + "&username=medicalrealadmin"
            + "&password=STRONGPASSWORD!@#";
    private final HttpHeaders tokenHeaders = new HttpHeaders();

    private User existingUser;
    private User nonExistingUser;
    private KeycloakUserDto keycloakUserDto;

    @BeforeEach
    void setUp() {
        // Set configuration fields
        ReflectionTestUtils.setField(keycloakService, "keycloakAuthServerUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(keycloakService, "realmName", "medical-realm");
        ReflectionTestUtils.setField(keycloakService, "clientId", "medical-backend");
        ReflectionTestUtils.setField(keycloakService, "adminUsername", "medicalrealadmin");
        ReflectionTestUtils.setField(keycloakService, "adminPassword", "STRONGPASSWORD!@#");

        // Inject the mocked RestTemplate
        ReflectionTestUtils.setField(keycloakService, "restTemplate", restTemplate);

        // Initialize test users
        existingUser = new User();
        existingUser.setId(1L);
        existingUser.setKeycloakUserId("user-123");
        existingUser.setRole("user");
        existingUser.setFirstName("John");
        existingUser.setLastName("Doe");
        existingUser.setEmail("john.doe@example.com");

        nonExistingUser = new User();
        nonExistingUser.setId(2L);
        nonExistingUser.setKeycloakUserId("user-999");
        nonExistingUser.setRole("user");
        nonExistingUser.setFirstName("Jane");
        nonExistingUser.setLastName("Smith");
        nonExistingUser.setEmail("jane.smith@example.com");

        // Set default headers for token retrieval
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        // Default stubbing for getAdminAccessToken to return a mock token
        lenient().when(restTemplate.postForEntity(
                eq(tokenUrl),
                argThat((HttpEntity<String> entity) ->
                        entity.getBody().equals(tokenBody) &&
                                entity.getHeaders().equals(tokenHeaders)
                ),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(
                Map.of("access_token", "mock-admin-token"),
                HttpStatus.OK
        ));
    }

    /**
     * Helper method to stub getAdminAccessToken with a successful response.
     */
    private void stubAdminAccessTokenSuccess(String accessToken) {
        lenient().when(restTemplate.postForEntity(
                eq(tokenUrl),
                argThat((HttpEntity<String> entity) ->
                        entity.getBody().equals(tokenBody) &&
                                entity.getHeaders().equals(tokenHeaders)
                ),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(
                Map.of("access_token", accessToken),
                HttpStatus.OK
        ));
    }

    /**
     * Helper method to stub getAdminAccessToken with a failure response.
     */
    private void stubAdminAccessTokenFailure(HttpStatus status) {
        lenient().when(restTemplate.postForEntity(
                eq(tokenUrl),
                argThat((HttpEntity<String> entity) ->
                        entity.getBody().equals(tokenBody) &&
                                entity.getHeaders().equals(tokenHeaders)
                ),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(null, status));
    }

    /**
     * Test successfully obtaining an admin access token.
     */
    @Test
    void getAdminAccessToken_Success() {
        // Arrange
        String adminToken = "admin-access-token";
        stubAdminAccessTokenSuccess(adminToken);

        // Act
        String result = keycloakService.getAdminAccessToken();

        // Assert
        assertEquals(adminToken, result);
        verify(restTemplate, times(1)).postForEntity(
                eq(tokenUrl),
                argThat((HttpEntity<String> entity) ->
                        entity.getBody().equals(tokenBody) &&
                                entity.getHeaders().equals(tokenHeaders)
                ),
                eq(Map.class)
        );
    }

    /**
     * Test failure when obtaining an admin access token.
     */
    @Test
    void getAdminAccessToken_Failure() {
        // Arrange
        stubAdminAccessTokenFailure(HttpStatus.BAD_REQUEST);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.getAdminAccessToken();
        });

        assertEquals("Failed to obtain admin access token from Keycloak", exception.getMessage());
        verify(restTemplate, times(1)).postForEntity(
                eq(tokenUrl),
                argThat((HttpEntity<String> entity) ->
                        entity.getBody().equals(tokenBody) &&
                                entity.getHeaders().equals(tokenHeaders)
                ),
                eq(Map.class)
        );
    }

    /**
     * Test successfully creating a user in Keycloak.
     */
    @Test
    void createUser_Success() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String password = "password123";
        String userId = "user-123";

        // Mock restTemplate.postForEntity for user creation
        String createUserUrl = "http://localhost:8080/admin/realms/medical-realm/users";
        when(restTemplate.postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // Mock restTemplate.exchange for getUserIdByUsername
        String searchUrl = "http://localhost:8080/admin/realms/medical-realm/users?username=" + username;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("firstName", "John");
        userMap.put("lastName", "Doe");
        userMap.put("emailVerified", true);

        when(restTemplate.exchange(
                eq(searchUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object[].class)
        )).thenReturn(new ResponseEntity<>(new Object[]{userMap}, HttpStatus.OK));

        // Act
        String createdUserId = keycloakService.createUser(username, email, password);

        // Assert
        assertEquals(userId, createdUserId);
        verify(restTemplate, times(1)).postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(searchUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object[].class)
        );
    }

    /**
     * Test failure when creating a user in Keycloak.
     */
    @Test
    void createUser_Failure() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String password = "password123";

        // Mock restTemplate.postForEntity for user creation to return BAD_REQUEST
        String createUserUrl = "http://localhost:8080/admin/realms/medical-realm/users";
        when(restTemplate.postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.createUser(username, email, password);
        });

        assertEquals("Failed to create user in Keycloak. Status: 400 BAD_REQUEST", exception.getMessage());
        verify(restTemplate, times(1)).postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        );
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(Object[].class)
        );
    }

    /**
     * Test successfully fetching a user by ID.
     */
    @Test
    void findUserById_Success() {
        // Arrange
        String userId = "user-123";

        // Stub getAdminAccessToken to return a mock token
        stubAdminAccessTokenSuccess("mock-admin-token");

        String userUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;
        String rolesUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId + "/role-mappings/realm";

        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", "johndoe");
        userMap.put("email", "john.doe@example.com");
        userMap.put("firstName", "John");
        userMap.put("lastName", "Doe");
        userMap.put("emailVerified", true);

        Map<String, Object> roleMap = new HashMap<>();
        roleMap.put("name", "user");
        roleMap.put("composite", false);
        roleMap.put("clientRole", false);
        roleMap.put("containerId", "medical-realm");

        when(restTemplate.exchange(
                eq(userUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(userMap, HttpStatus.OK));

        when(restTemplate.exchange(
                eq(rolesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(new ResponseEntity<>(List.of(roleMap), HttpStatus.OK));

        // Act
        KeycloakUserDto result = keycloakService.findUserById(userId);

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("johndoe", result.getUsername());
        assertEquals("john.doe@example.com", result.getEmail());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertTrue(result.isEmailVerified());
        assertEquals("user", result.getRole());

        verify(restTemplate, times(1)).exchange(
                eq(userUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(rolesUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        );
    }

    /**
     * Test failure when finding a user by ID.
     */
    @Test
    void findUserById_Failure() {
        // Arrange
        String userId = "user-999";

        // Stub getAdminAccessToken to return a mock token
        stubAdminAccessTokenSuccess("mock-admin-token");

        String userUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        when(restTemplate.exchange(
                eq(userUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.findUserById(userId);
        });

        assertEquals("Failed to fetch user details from Keycloak", exception.getMessage());

        verify(restTemplate, times(1)).exchange(
                eq(userUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
        verify(restTemplate, never()).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                eq(List.class)
        );
    }

    /**
     * Test successfully assigning a role to a user.
     */
    @Test
    void assignRole_Success() {
        // Arrange
        String userId = "user-123";
        String roleName = "doctor";

        stubAdminAccessTokenSuccess("mock-admin-token");

        String roleUrl = "http://localhost:8080/admin/realms/medical-realm/roles/" + roleName;
        String assignUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId + "/role-mappings/realm";

        Map<String, Object> roleRepresentation = new HashMap<>();
        roleRepresentation.put("name", roleName);
        roleRepresentation.put("composite", false);
        roleRepresentation.put("clientRole", false);
        roleRepresentation.put("containerId", "medical-realm");

        // Mock restTemplate.exchange for fetching role
        when(restTemplate.exchange(
                eq(roleUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(roleRepresentation, HttpStatus.OK));

        // Mock restTemplate.exchange for assigning role
        when(restTemplate.exchange(
                eq(assignUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        // Act
        keycloakService.assignRole(userId, roleName);

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq(roleUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(assignUrl),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    /**
     * Test failure when assigning a role to a user.
     */
    @Test
    void assignRole_Failure_RoleNotFound() {
        // Arrange
        String userId = "user-123";
        String roleName = "nonexistentrole";

        stubAdminAccessTokenSuccess("mock-admin-token");

        String roleUrl = "http://localhost:8080/admin/realms/medical-realm/roles/" + roleName;

        // Mock restTemplate.exchange for fetching role to return NOT_FOUND
        when(restTemplate.exchange(
                eq(roleUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        )).thenReturn(new ResponseEntity<>(null, HttpStatus.NOT_FOUND));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.assignRole(userId, roleName);
        });

        assertEquals("Role 'nonexistentrole' not found in Keycloak", exception.getMessage());

        verify(restTemplate, times(1)).exchange(
                eq(roleUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Map.class)
        );
        verify(restTemplate, never()).exchange(
                anyString(),
                eq(HttpMethod.POST),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    /**
     * Test successfully creating a user with additional details.
     */
    @Test
    void createUser_WithAdditionalDetails_Success() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";
        String userId = "user-123";

        // Mock restTemplate.postForEntity for user creation
        String createUserUrl = "http://localhost:8080/admin/realms/medical-realm/users";
        when(restTemplate.postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.CREATED));

        // Mock restTemplate.exchange for getUserIdByUsername
        String searchUrl = "http://localhost:8080/admin/realms/medical-realm/users?username=" + username;
        Map<String, Object> userMap = new HashMap<>();
        userMap.put("id", userId);
        userMap.put("username", username);
        userMap.put("email", email);
        userMap.put("firstName", firstName);
        userMap.put("lastName", lastName);
        userMap.put("emailVerified", true);

        when(restTemplate.exchange(
                eq(searchUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object[].class)
        )).thenReturn(new ResponseEntity<>(new Object[]{userMap}, HttpStatus.OK));

        // Act
        String createdUserId = keycloakService.createUser(username, email, password, firstName, lastName);

        // Assert
        assertEquals(userId, createdUserId);
        verify(restTemplate, times(1)).postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(searchUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(Object[].class)
        );
    }

    /**
     * Test failure when creating a user with additional details.
     */
    @Test
    void createUser_WithAdditionalDetails_Failure() {
        // Arrange
        String username = "johndoe";
        String email = "john.doe@example.com";
        String password = "password123";
        String firstName = "John";
        String lastName = "Doe";

        // Mock restTemplate.postForEntity for user creation to return BAD_REQUEST
        String createUserUrl = "http://localhost:8080/admin/realms/medical-realm/users";
        when(restTemplate.postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.createUser(username, email, password, firstName, lastName);
        });

        // Assert the exception message
        assertEquals("Failed to create user in Keycloak. Status: 400 BAD_REQUEST", exception.getMessage());

        // Verify that the user creation endpoint was called once
        verify(restTemplate, times(1)).postForEntity(
                eq(createUserUrl),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Removed verifyNoMoreInteractions(restTemplate) to allow interactions from getAdminAccessToken
    }






    /**
     * Test failure when setting a user's emailVerified status.
     */
    @Test
    void setUserEmailVerified_Failure() {
        // Arrange
        String userId = "user-123";
        boolean isVerified = true;

        // Stub getAdminAccessToken to return a mock token
        stubAdminAccessTokenSuccess("mock-admin-token");

        String updateUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        // Mock restTemplate.exchange for updating emailVerified to return BAD_REQUEST
        when(restTemplate.exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.setUserEmailVerified(userId, isVerified);
        });

        assertEquals("Failed to update emailVerified in Keycloak", exception.getMessage());

        verify(restTemplate, times(1)).exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Ensure userService.updateUser is never called
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test successfully deleting a user.
     */
    @Test
    void deleteUser_Success() {
        // Arrange
        String userId = "user-123";

        String deleteUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        // Mock deleteAssociatedData
        doNothing().when(keycloakService).deleteAssociatedData(userId);

        // Mock restTemplate.exchange for deleting user
        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        // Act
        keycloakService.deleteUser(userId);

        // Assert
        verify(keycloakService, times(1)).deleteAssociatedData(userId);
        verify(restTemplate, times(1)).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    /**
     * Test failure when deleting a user.
     */
    @Test
    void deleteUser_Failure() {
        // Arrange
        String userId = "user-123";

        String deleteUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        // Mock deleteAssociatedData
        doNothing().when(keycloakService).deleteAssociatedData(userId);

        // Mock restTemplate.exchange for deleting user to return BAD_REQUEST
        when(restTemplate.exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.deleteUser(userId);
        });

        assertEquals("Failed to delete user in Keycloak", exception.getMessage());

        verify(keycloakService, times(1)).deleteAssociatedData(userId);
        verify(restTemplate, times(1)).exchange(
                eq(deleteUrl),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        );
    }

    /**
     * Test successfully updating user details.
     */
    @Test
    void updateUserDetails_Success() {
        // Arrange
        String userId = "user-123";
        KeycloakUserDto dto = new KeycloakUserDto();
        dto.setEmail("new.email@example.com");
        dto.setFirstName("Johnny");
        dto.setLastName("Doe");
        dto.setUsername("johnnydoe");
        dto.setEmailVerified(true);
        dto.setEgn("1234567890"); // Assuming EGN is a field in User

        String updateUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        // Mock restTemplate.exchange for updating user details
        when(restTemplate.exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.NO_CONTENT));

        // Mock userService.findByKeycloakUserId
        when(userService.findByKeycloakUserId(userId)).thenReturn(existingUser);

        // Mock userService.updateUser (assuming it's a void method)
        doNothing().when(userService).updateUser(existingUser);

        // Act
        keycloakService.updateUserDetails(userId, dto);

        // Assert
        verify(restTemplate, times(1)).exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );

        verify(userService, times(1)).findByKeycloakUserId(userId);
        verify(userService, times(1)).updateUser(existingUser);

        assertEquals("new.email@example.com", existingUser.getEmail());
        assertEquals("Johnny", existingUser.getFirstName());
        assertEquals("Doe", existingUser.getLastName());
        // Assuming username is stored elsewhere or managed by Keycloak
        assertEquals("1234567890", existingUser.getEgn());
    }

    /**
     * Test failure when updating user details.
     */
    @Test
    void updateUserDetails_Failure() {
        // Arrange
        String userId = "user-123";
        KeycloakUserDto dto = new KeycloakUserDto();
        dto.setEmail("new.email@example.com");
        dto.setFirstName("Johnny");
        dto.setLastName("Doe");
        dto.setUsername("johnnydoe");
        dto.setEmailVerified(true);
        dto.setEgn("1234567890");

        String updateUrl = "http://localhost:8080/admin/realms/medical-realm/users/" + userId;

        // Mock restTemplate.exchange for updating user details to return BAD_REQUEST
        when(restTemplate.exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.BAD_REQUEST));

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            keycloakService.updateUserDetails(userId, dto);
        });

        assertEquals("Failed to update user details in Keycloak", exception.getMessage());

        verify(restTemplate, times(1)).exchange(
                eq(updateUrl),
                eq(HttpMethod.PUT),
                any(HttpEntity.class),
                eq(Void.class)
        );

        // Ensure userService.updateUser is never called
        verify(userService, never()).updateUser(any(User.class));
    }

    /**
     * Test successfully deleting associated data for a user.
     */
    @Test
    void deleteAssociatedData_Success() {
        // Arrange
        String userId = "user-123";

        when(userService.existsByKeycloakId(userId)).thenReturn(true);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(true);
        when(patientService.existsByKeycloakId(userId)).thenReturn(false);

        // Act
        keycloakService.deleteAssociatedData(userId);

        // Assert
        verify(userService, times(1)).existsByKeycloakId(userId);
        verify(userService, times(1)).deleteByKeycloakUserId(userId);
        verify(doctorService, times(1)).existsByKeycloakId(userId);
        verify(doctorService, times(1)).deleteByKeycloakUserId(userId);
        verify(patientService, times(1)).existsByKeycloakId(userId);
        verify(patientService, never()).deleteByKeycloakUserId(anyString());
    }

    /**
     * Test deleting associated data when some services do not have the user.
     */
    @Test
    void deleteAssociatedData_PartialExistence() {
        // Arrange
        String userId = "user-456";

        when(userService.existsByKeycloakId(userId)).thenReturn(false);
        when(doctorService.existsByKeycloakId(userId)).thenReturn(true);
        when(patientService.existsByKeycloakId(userId)).thenReturn(false);

        // Act
        keycloakService.deleteAssociatedData(userId);

        // Assert
        verify(userService, times(1)).existsByKeycloakId(userId);
        verify(userService, never()).deleteByKeycloakUserId(anyString());
        verify(doctorService, times(1)).existsByKeycloakId(userId);
        verify(doctorService, times(1)).deleteByKeycloakUserId(userId);
        verify(patientService, times(1)).existsByKeycloakId(userId);
        verify(patientService, never()).deleteByKeycloakUserId(anyString());
    }

    /**
     * Test successfully fetching all users from Keycloak and syncing.
     */
    @Test
    void syncUsers_Success() {
        // Arrange
        String adminToken = "mock-admin-token";
        stubAdminAccessTokenSuccess(adminToken);

        String usersUrl = "http://localhost:8080/admin/realms/medical-realm/users?max=1000";

        List<Map<String, Object>> keycloakUsers = Arrays.asList(
                Map.of("id", "user-123", "username", "johndoe", "email", "john.doe@example.com", "firstName", "John", "lastName", "Doe", "emailVerified", true),
                Map.of("id", "user-456", "username", "janesmith", "email", "jane.smith@example.com", "firstName", "Jane", "lastName", "Smith", "emailVerified", false)
        );

        when(restTemplate.exchange(
                eq(usersUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(new ResponseEntity<>(keycloakUsers, HttpStatus.OK));

        // Mock fetching roles for each user
        String rolesUrlUser1 = "http://localhost:8080/admin/realms/medical-realm/users/user-123/role-mappings/realm";
        String rolesUrlUser2 = "http://localhost:8080/admin/realms/medical-realm/users/user-456/role-mappings/realm";

        Map<String, Object> roleUser1 = Map.of(
                "name", "user",
                "composite", false,
                "clientRole", false,
                "containerId", "medical-realm"
        );

        Map<String, Object> roleUser2 = Map.of(
                "name", "patient",
                "composite", false,
                "clientRole", false,
                "containerId", "medical-realm"
        );

        when(restTemplate.exchange(
                eq(rolesUrlUser1),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(new ResponseEntity<>(List.of(roleUser1), HttpStatus.OK));

        when(restTemplate.exchange(
                eq(rolesUrlUser2),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        )).thenReturn(new ResponseEntity<>(List.of(roleUser2), HttpStatus.OK));

        // Mock local users
        User localUser = new User();
        localUser.setId(1L);
        localUser.setKeycloakUserId("user-123");
        localUser.setUsername("johndoe");
        localUser.setEmail("john.doe@example.com");
        localUser.setFirstName("John");
        localUser.setLastName("Doe");
        localUser.setRole("user");

        User localUserToDelete = new User();
        localUserToDelete.setId(2L);
        localUserToDelete.setKeycloakUserId("user-999");
        localUserToDelete.setUsername("janedoe");
        localUserToDelete.setEmail("jane.doe@example.com");
        localUserToDelete.setFirstName("Jane");
        localUserToDelete.setLastName("Doe");
        localUserToDelete.setRole("user");

        when(userService.findAll()).thenReturn(List.of(localUser, localUserToDelete));

        // Mock userService.createUser for user-456
        when(userService.createUser(argThat(user -> "user-456".equals(user.getKeycloakUserId()))))
                .thenReturn(new User());

        // Mock userService.deleteUser for user-999
        doNothing().when(userService).deleteUser(localUserToDelete.getId());

        // Act
        keycloakService.syncUsers();

        // Assert
        // Verify restTemplate calls
        verify(restTemplate, times(1)).exchange(
                eq(usersUrl),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(rolesUrlUser1),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        );
        verify(restTemplate, times(1)).exchange(
                eq(rolesUrlUser2),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(List.class)
        );

        // Verify that userService.createUser was called for user-456
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userService, times(1)).createUser(userCaptor.capture());

        User newUser = userCaptor.getValue();
        assertEquals("user-456", newUser.getKeycloakUserId());
        assertEquals("janesmith", newUser.getUsername());
        assertEquals("jane.smith@example.com", newUser.getEmail());
        assertEquals("Jane", newUser.getFirstName());
        assertEquals("Smith", newUser.getLastName());
        assertEquals("patient", newUser.getRole());

        // Verify that userService.deleteUser was called for user-999
        verify(userService, times(1)).deleteUser(localUserToDelete.getId());
    }
}
