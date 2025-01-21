package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.exception.UserNotFoundException;
import bg.nbu.medicalrecords.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setKeycloakUserId("kc-12345");
        user.setEgn("1234567890");
        user.setRole("user");
    }

    @Test
    void createUser_Success() {
        // Arrange
        when(userRepository.save(user)).thenReturn(user);

        // Act
        User createdUser = userService.createUser(user);

        // Assert
        assertNotNull(createdUser);
        assertEquals("kc-12345", createdUser.getKeycloakUserId());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void findByKeycloakUserId_Success() {
        // Arrange
        when(userRepository.findByKeycloakUserId("kc-12345")).thenReturn(user);

        // Act
        User foundUser = userService.findByKeycloakUserId("kc-12345");

        // Assert
        assertNotNull(foundUser);
        assertEquals("kc-12345", foundUser.getKeycloakUserId());
        verify(userRepository, times(1)).findByKeycloakUserId("kc-12345");
    }

    @Test
    void findByKeycloakUserId_Failure_UserNotFound() {
        // Arrange
        when(userRepository.findByKeycloakUserId("kc-12345")).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findByKeycloakUserId("kc-12345"));
        verify(userRepository, times(1)).findByKeycloakUserId("kc-12345");
    }

    @Test
    void findByEgn_Success() {
        // Arrange
        when(userRepository.findByEgn("1234567890")).thenReturn(user);
        when(userRepository.findByKeycloakUserId("kc-12345")).thenReturn(user);

        // Act
        User foundUser = userService.findByEgn("1234567890");

        // Assert
        assertNotNull(foundUser);
        assertEquals("1234567890", foundUser.getEgn());
        verify(userRepository, times(1)).findByEgn("1234567890");
    }

    @Test
    void findByEgn_Failure_UserNotFound() {
        // Arrange
        when(userRepository.findByEgn("1234567890")).thenReturn(null);

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.findByEgn("1234567890"));
        verify(userRepository, times(1)).findByEgn("1234567890");
    }

    @Test
    void existsByKeycloakId_Success() {
        // Arrange
        when(userRepository.existsByKeycloakUserId("kc-12345")).thenReturn(true);

        // Act
        boolean exists = userService.existsByKeycloakId("kc-12345");

        // Assert
        assertTrue(exists);
        verify(userRepository, times(1)).existsByKeycloakUserId("kc-12345");
    }

    @Test
    void deleteByKeycloakUserId_Success() {
        // Arrange
        when(userRepository.findByKeycloakUserId("kc-12345")).thenReturn(user);
        doNothing().when(userRepository).delete(user);

        // Act
        userService.deleteByKeycloakUserId("kc-12345");

        // Assert
        verify(userRepository, times(1)).findByKeycloakUserId("kc-12345");
        verify(userRepository, times(1)).delete(user);
    }

    @Test
    void assignRole_Success() {
        // Arrange
        when(userRepository.findByKeycloakUserId("kc-12345")).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.assignRole("kc-12345", "admin");

        // Assert
        assertEquals("admin", user.getRole());
        verify(userRepository, times(1)).findByKeycloakUserId("kc-12345");
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void findAll_Success() {
        // Arrange
        List<User> users = new ArrayList<>();
        users.add(user);
        when(userRepository.findAll()).thenReturn(users);

        // Act
        List<User> foundUsers = userService.findAll();

        // Assert
        assertNotNull(foundUsers);
        assertEquals(1, foundUsers.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void deleteUser_Success() {
        // Arrange
        doNothing().when(userRepository).deleteById(1L);

        // Act
        userService.deleteUser(1L);

        // Assert
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void updateUser_Success() {
        // Arrange
        when(userRepository.save(user)).thenReturn(user);

        // Act
        userService.updateUser(user);

        // Assert
        verify(userRepository, times(1)).save(user);
    }
}
