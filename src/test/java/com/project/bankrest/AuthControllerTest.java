package com.project.bankrest;

import com.project.bankrest.Controller.AuthController;
import com.project.bankrest.DTO.AuthRequest;
import com.project.bankrest.DTO.SignupRequest;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.UserRepository;
import com.project.bankrest.Security.JwtUtils;
import com.project.bankrest.Services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtils jwtUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserService userService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private AuthController authController;

    @Test
    void testSignupSuccess() {

        SignupRequest request = new SignupRequest();
        request.setUsername("newUser");
        request.setPassword("pass");

        doNothing().when(userService).register(any(SignupRequest.class));

        ResponseEntity<String> response = authController.signup(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userService, times(1)).register(request);
    }

    @Test
    void testSignupConflict() {
        SignupRequest request = new SignupRequest();
        request.setUsername("existing");
        String errorMessage = "User exists";

        doThrow(new IllegalArgumentException(errorMessage))
                .when(userService).register(any(SignupRequest.class));

        ResponseEntity<String> response = authController.signup(request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void testSignupUnexpectedError() {
        SignupRequest request = new SignupRequest();
        request.setUsername("errorUser");
        String errorMessage = "Unexpected error occurred";

        doThrow(new RuntimeException(errorMessage))
                .when(userService).register(any(SignupRequest.class));

        ResponseEntity<String> response = authController.signup(request);

        assertEquals(408, response.getStatusCode().value());
        assertEquals(errorMessage, response.getBody());
    }

    @Test
    void testLoginSuccess() {
        AuthRequest req = new AuthRequest();
        req.setUsername("admin");
        req.setPassword("pass");

        Authentication authentication = mock(Authentication.class);
        String mockToken = "mock_token";

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(jwtUtils.generateJwtToken(userDetails)).thenReturn(mockToken);

        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        @SuppressWarnings("unchecked")
        Map<String, String> responseBody = (Map<String, String>) response.getBody();
        assertNotNull(responseBody);
        assertEquals(mockToken, responseBody.get("token"));
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginFail() {
        AuthRequest req = new AuthRequest();
        req.setUsername("bad");
        req.setPassword("bad");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new RuntimeException("Bad Credentials"));

        ResponseEntity<?> response = authController.login(req);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void testRegisterUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("raw");

        User savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("test");
        savedUser.setPassword("encoded_password");

        when(passwordEncoder.encode("raw")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        ResponseEntity<User> response = authController.register(user);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("encoded_password", response.getBody().getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(userCaptor.capture());

        assertEquals("encoded_password", userCaptor.getValue().getPassword());

        verify(passwordEncoder, times(1)).encode("raw");
    }
}