package com.project.bankrest;

import com.project.bankrest.Entity.User;
import com.project.bankrest.Entity.Role;
import com.project.bankrest.Security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class JwtUtilsTest {

    private JwtUtils jwtUtils;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();

        user = new User();
        user.setId(5L);
        user.setUsername("testUser");
        user.setPassword("password");
        user.setRole(Role.ADMIN);
        user.setName("Test Name");
    }

    @Test
    void testGenerateJwtToken_NotNull() {
        String token = jwtUtils.generateJwtToken(user);
        assertNotNull(token);
    }

    @Test
    void testValidateJwtToken_Valid() {
        String token = jwtUtils.generateJwtToken(user);
        assertTrue(jwtUtils.validateJwtToken(token));
    }

    @Test
    void testValidateJwtToken_Invalid() {
        String invalid = "invalidToken123";
        assertFalse(jwtUtils.validateJwtToken(invalid));
    }

    @Test
    void testGetUsernameFromJwtToken() {
        String token = jwtUtils.generateJwtToken(user);
        String username = jwtUtils.getUsernameFromJwtToken(token);

        assertEquals(user.getUsername(), username);
    }

    @Test
    void testTokenContainsCustomClaims() {
        String token = jwtUtils.generateJwtToken(user);

        String username = jwtUtils.getUsernameFromJwtToken(token);
        assertEquals("testUser", username);

        assertTrue(jwtUtils.validateJwtToken(token));
    }
}
