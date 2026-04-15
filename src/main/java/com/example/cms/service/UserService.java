package com.example.cms.service;

import com.example.cms.entity.User;
import com.example.cms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    private final UserRepository userRepository;

    /**
     * Register a new user (plain-text password for simplicity)
     * For production: use BCryptPasswordEncoder
     */
    public User register(String name, String email, String password, User.Role role) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered: " + email);
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password); // NOTE: hash in production!
        user.setRole(role != null ? role : User.Role.USER);
        return userRepository.save(user);
    }

    /**
     * Authenticate user by email and password
     */
    @Transactional(readOnly = true)
    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    /**
     * Find user by ID
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by Email
     */
    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Get all users (Admin only)
     */
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Delete user by ID (Admin only)
     */
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    /**
     * Update user role (Admin only)
     */
    public User updateRole(Long userId, User.Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        user.setRole(role);
        return userRepository.save(user);
    }
}
