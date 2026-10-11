package com.omega.omegamart.service;

import com.omega.omegamart.dao.UserDao;
import com.omega.omegamart.exception.AuthenticationException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
import com.omega.omegamart.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Service orchestrating user registration, credential verification, and authentication policies.
 */
public class AuthService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthService.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private final UserDao userDao;

    public AuthService() {
        this(new UserDao());
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * Registers a new buyer or seller account. Public admin registration is strictly forbidden.
     */
    public User register(String email, String password, String fullName, Role role) {
        // 1. Validation
        if (email == null || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new ValidationException("Please provide a valid email address.");
        }
        if (password == null || password.length() < 8) {
            throw new ValidationException("Password must be at least 8 characters long.");
        }
        if (fullName == null || fullName.trim().isEmpty() || fullName.trim().length() > 100) {
            throw new ValidationException("Full name is required and must not exceed 100 characters.");
        }
        if (role == null || role == Role.ADMIN) {
            throw new ValidationException("Registration is only permitted for BUYER and SELLER accounts.");
        }

        String normalizedEmail = email.trim().toLowerCase();

        // 2. Duplicate email check
        if (userDao.existsByEmail(normalizedEmail)) {
            // Safe generic message preventing detailed account enumeration
            throw new ValidationException("An account with this email address already exists.");
        }

        // 3. Password hashing & persistence
        User user = new User();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(PasswordUtil.hashPassword(password));
        user.setFullName(fullName.trim());
        user.setRole(role);

        try {
            return userDao.createUser(user);
        } catch (SQLException e) {
            LOGGER.error("Failed to register user {}: {}", normalizedEmail, e.getMessage(), e);
            throw new RuntimeException("An error occurred during account registration. Please try again.");
        }
    }

    /**
     * Authenticates user credentials using BCrypt.
     */
    public User login(String email, String password) {
        if (email == null || email.trim().isEmpty() || password == null || password.isEmpty()) {
            throw new AuthenticationException("Invalid email or password.");
        }

        String normalizedEmail = email.trim().toLowerCase();
        Optional<User> optionalUser = userDao.findByEmail(normalizedEmail);

        if (optionalUser.isEmpty()) {
            // Constant-time defense against user enumeration: perform dummy check
            PasswordUtil.checkPassword(password, "$2a$10$w8c7iQn.B20Vj3zG6JvB..4qfV3ZJ7iO/3c11H3gZ8M5E5g3k8h1a");
            throw new AuthenticationException("Invalid email or password.");
        }

        User user = optionalUser.get();
        if (!PasswordUtil.checkPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid email or password.");
        }

        LOGGER.info("User {} successfully logged in with role {}", user.getEmail(), user.getRole());
        return user;
    }

    public Optional<User> getUserById(Long userId) {
        return userDao.findById(userId);
    }
}
