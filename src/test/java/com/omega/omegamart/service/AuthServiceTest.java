package com.omega.omegamart.service;

import com.omega.omegamart.dao.UserDao;
import com.omega.omegamart.exception.AuthenticationException;
import com.omega.omegamart.exception.ValidationException;
import com.omega.omegamart.model.Role;
import com.omega.omegamart.model.User;
import com.omega.omegamart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserDao userDao;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userDao);
    }

    @Test
    @DisplayName("Should successfully register a new buyer")
    void testRegisterBuyerSuccess() throws SQLException {
        when(userDao.existsByEmail("alice@example.com")).thenReturn(false);
        when(userDao.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });

        User registered = authService.register("alice@example.com", "Password@123", "Alice Walker", Role.BUYER);

        assertNotNull(registered);
        assertEquals("alice@example.com", registered.getEmail());
        assertEquals(Role.BUYER, registered.getRole());
        assertTrue(PasswordUtil.checkPassword("Password@123", registered.getPasswordHash()));
        verify(userDao, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should reject registration if email already exists")
    void testRegisterDuplicateEmail() throws SQLException {
        when(userDao.existsByEmail("existing@example.com")).thenReturn(true);

        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("existing@example.com", "Password@123", "Duplicate User", Role.BUYER)
        );

        assertTrue(ex.getMessage().contains("already exists"));
        verify(userDao, never()).createUser(any(User.class));
    }

    @Test
    @DisplayName("Should reject public admin registration")
    void testRegisterAdminForbidden() {
        ValidationException ex = assertThrows(ValidationException.class, () ->
                authService.register("newadmin@example.com", "Password@123", "Fake Admin", Role.ADMIN)
        );

        assertTrue(ex.getMessage().contains("Registration is only permitted for BUYER and SELLER"));
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void testRegisterInvalidEmail() {
        assertThrows(ValidationException.class, () ->
                authService.register("invalid-email", "Password@123", "Name", Role.BUYER)
        );
    }

    @Test
    @DisplayName("Should successfully authenticate valid credentials")
    void testLoginSuccess() {
        String hashed = PasswordUtil.hashPassword("CorrectPass@123");
        User user = new User(1L, "user@example.com", hashed, "Valid User", Role.BUYER);

        when(userDao.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        User loggedIn = authService.login("user@example.com", "CorrectPass@123");
        assertNotNull(loggedIn);
        assertEquals(1L, loggedIn.getId());
    }

    @Test
    @DisplayName("Should reject login with invalid password")
    void testLoginInvalidPassword() {
        String hashed = PasswordUtil.hashPassword("CorrectPass@123");
        User user = new User(1L, "user@example.com", hashed, "Valid User", Role.BUYER);

        when(userDao.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () ->
                authService.login("user@example.com", "WrongPassword")
        );
    }
}
