package bg.nbu.medicalrecords.service;

import bg.nbu.medicalrecords.domain.User;
import bg.nbu.medicalrecords.exception.UserNotFoundException;
import bg.nbu.medicalrecords.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(User user) {
        userRepository.save(user);
        return user;
    }

    public User findByKeycloakUserId(String kcUserId) {
        User user = userRepository.findByKeycloakUserId(kcUserId);
        if (user == null) {
            throw new UserNotFoundException("User not found with Keycloak ID: " + kcUserId);
        }
        return user;
    }

    public User findByEgn(String egn) {
        User user = userRepository.findByEgn(egn);
        if (user == null) {
            throw new UserNotFoundException("User not found with EGN: " + egn);
        }
        return findByKeycloakUserId(user.getKeycloakUserId());
    }

    public boolean existsByKeycloakId(String userId) {
        return userRepository.existsByKeycloakUserId(userId);
    }

    public void deleteByKeycloakUserId(String userId) {
        User user = findByKeycloakUserId(userId);
        userRepository.delete(user);
    }

    public void assignRole(String userId, String role) {
        User user = findByKeycloakUserId(userId);
        user.setRole(role);
        userRepository.save(user);
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    public void updateUser(User user) {
        userRepository.save(user);
    }
}