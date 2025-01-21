package bg.nbu.medicalrecords.reposiory;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        // Create and save a sample User
        sampleUser = new User();
        sampleUser.setKeycloakUserId("keycloak-user-id-123");
        sampleUser.setEgn("1234567890");
        sampleUser.setUsername("testuser");
        sampleUser.setEmail("testuser@example.com");
        sampleUser.setFirstName("Test");
        sampleUser.setLastName("User");
        sampleUser.setRole("ADMIN");
        userRepository.save(sampleUser);
    }

    @Test
    void save_ShouldPersistUser() {
        User user = new User();
        user.setKeycloakUserId("keycloak-user-id-456");
        user.setEgn("0987654321");
        user.setUsername("newuser");
        user.setEmail("newuser@example.com");
        user.setFirstName("New");
        user.setLastName("User");
        user.setRole("USER");

        User savedUser = userRepository.save(user);

        assertNotNull(savedUser.getId());
        assertEquals("newuser", savedUser.getUsername());
    }

    @Test
    void findByKeycloakUserId_ShouldReturnUser() {
        User foundUser = userRepository.findByKeycloakUserId("keycloak-user-id-123");

        assertNotNull(foundUser);
        assertEquals(sampleUser.getKeycloakUserId(), foundUser.getKeycloakUserId());
        assertEquals(sampleUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void findByEgn_ShouldReturnUser() {
        User foundUser = userRepository.findByEgn("1234567890");

        assertNotNull(foundUser);
        assertEquals(sampleUser.getEgn(), foundUser.getEgn());
        assertEquals(sampleUser.getUsername(), foundUser.getUsername());
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnTrue() {
        boolean exists = userRepository.existsByKeycloakUserId("keycloak-user-id-123");

        assertTrue(exists);
    }

    @Test
    void existsByKeycloakUserId_ShouldReturnFalseForNonexistentId() {
        boolean exists = userRepository.existsByKeycloakUserId("nonexistent-id");

        assertFalse(exists);
    }

    @Test
    void deleteById_ShouldRemoveUser() {
        assertTrue(userRepository.existsById(sampleUser.getId()));

        userRepository.deleteById(sampleUser.getId());

        Optional<User> deletedUser = userRepository.findById(sampleUser.getId());
        assertTrue(deletedUser.isEmpty());
    }
}
