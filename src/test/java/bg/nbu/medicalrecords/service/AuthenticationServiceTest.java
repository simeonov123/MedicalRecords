package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthenticationService authenticationService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * Test successfully retrieving the current user when the principal is a valid JWT.
     */
    @Test
    void getCurrentUser_Success() {
        // Arrange
        String keycloakUserId = "user-123";

        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(keycloakUserId);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            User user = new User();
            user.setId(1L);
            user.setKeycloakUserId(keycloakUserId);
            user.setEgn("1234567890");
            user.setUsername("testuser");
            user.setEmail("test@example.com");
            user.setFirstName("Test");
            user.setLastName("User");
            user.setRole("user");

            when(userRepository.findByKeycloakUserId(keycloakUserId)).thenReturn(user);

            // Act
            User result = authenticationService.getCurrentUser();

            // Assert
            assertNotNull(result);
            assertEquals(user, result);

            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, times(1));
            verify(authentication, times(1)).getPrincipal();
            verify(jwt, times(1)).getClaim("sub");
            verify(userRepository, times(1)).findByKeycloakUserId(keycloakUserId);
        }
    }

    /**
     * Test retrieving the current user when the principal is not a JWT, expecting an exception.
     */
    @Test
    void getCurrentUser_PrincipalNotJwt() {
        // Arrange
        Object principal = "Not a JWT";

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(principal);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            // Act & Assert
            IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
                authenticationService.getCurrentUser();
            });

            assertEquals("Principal is not a JWT instance", exception.getMessage());

            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, times(1));
            verify(authentication, times(1)).getPrincipal();
            // Removed the following line as 'jwt' is not defined in this test
            // verify(jwt, never()).getClaim(anyString());
            verify(userRepository, never()).findByKeycloakUserId(anyString());
        }
    }

    /**
     * Test retrieving the current user when the JWT does not contain the 'sub' claim, expecting null.
     */
    @Test
    void getCurrentUser_JwtWithoutSubClaim() {
        // Arrange
        Jwt jwt = mock(Jwt.class);
        when(jwt.getClaim("sub")).thenReturn(null);

        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(jwt);

        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);

        try (MockedStatic<SecurityContextHolder> mockedSecurityContextHolder = mockStatic(SecurityContextHolder.class)) {
            mockedSecurityContextHolder.when(SecurityContextHolder::getContext).thenReturn(securityContext);

            when(userRepository.findByKeycloakUserId(null)).thenReturn(null);

            // Act
            User result = authenticationService.getCurrentUser();

            // Assert
            assertNull(result);

            mockedSecurityContextHolder.verify(SecurityContextHolder::getContext, times(1));
            verify(authentication, times(1)).getPrincipal();
            verify(jwt, times(1)).getClaim("sub");
            verify(userRepository, times(1)).findByKeycloakUserId(null);
        }
    }
}
