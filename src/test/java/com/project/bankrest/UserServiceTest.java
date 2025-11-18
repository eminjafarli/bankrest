package com.project.bankrest;

import com.project.bankrest.DTO.SignupRequest;
import com.project.bankrest.Entity.Role;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.UserRepository;

import com.project.bankrest.Services.UserService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JdbcTemplate jdbcTemplate;
    @Mock
    private EntityManager entityManager;

    @InjectMocks
    @Spy
    private UserService userService;

    private User existingUser;
    private SignupRequest validSignupRequest;

    private final String ENCODED_PASSWORD = "encoded_password_hash";

    @BeforeEach
    void setUp() {
        existingUser = User.builder()
                .id(1L)
                .username("existinguser")
                .password(ENCODED_PASSWORD)
                .name("John Doe")
                .role(Role.USER)
                .build();

        validSignupRequest = new SignupRequest("newuser", "securepassword123", "Jane Smith");
    }

    @Test
    void register_ValidRequest_ShouldSaveNewUser() {
        when(userRepository.findByUsername(validSignupRequest.getUsername())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validSignupRequest.getPassword())).thenReturn(ENCODED_PASSWORD);
        userService.register(validSignupRequest);

        verify(userRepository, times(1)).save(argThat(user ->
                user.getUsername().equals(validSignupRequest.getUsername()) &&
                        user.getPassword().equals(ENCODED_PASSWORD) &&
                        user.getRole().equals(Role.USER)
        ));
    }

    @Test
    void register_UsernameExists_ShouldThrowRuntimeException() {
        when(userRepository.findByUsername(validSignupRequest.getUsername())).thenReturn(Optional.of(existingUser));
        assertThrows(RuntimeException.class, () -> {
            userService.register(validSignupRequest);
        }, "Username already exists.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void register_ShortPassword_ShouldThrowIllegalArgumentException() {
        validSignupRequest.setPassword("short");
        when(userRepository.findByUsername(validSignupRequest.getUsername())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            userService.register(validSignupRequest);
        }, "Password must contain at least 8 letters.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_UsernameExists_ShouldThrowRuntimeException() {
        when(userRepository.existsByUsername(existingUser.getUsername())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            userService.addUser(existingUser);
        }, "Username already exists.");

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void addUser_RoleIsNull_ShouldDefaultToUserRole() {
        User newUser = User.builder().username("new").role(null).build();
        when(userRepository.existsByUsername(newUser.getUsername())).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(newUser);

        User savedUser = userService.addUser(newUser);

        assertEquals(Role.USER, savedUser.getRole());

        verify(userRepository, times(1)).save(argThat(user ->
                user.getRole().equals(Role.USER)
        ));
    }

    @Test
    void updateUser_UpdateNameAndUsername_ShouldSaveUpdatedUser() {
        User updateData = User.builder()
                .username("new_username")
                .name("Jane Updated")
                .build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(1L, updateData);

        assertEquals("new_username", result.getUsername());
        assertEquals("Jane Updated", result.getName());

        assertEquals(ENCODED_PASSWORD, result.getPassword());

        verify(userRepository, times(1)).save(existingUser);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void updateUser_UpdatePassword_ShouldEncodeAndSave() {
        User updateData = User.builder().password("new_secure_pass").build();

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(passwordEncoder.encode(updateData.getPassword())).thenReturn("new_encoded_hash");
        when(userRepository.save(any(User.class))).thenReturn(existingUser);

        User result = userService.updateUser(1L, updateData);

        assertEquals("new_encoded_hash", result.getPassword());

        verify(passwordEncoder, times(1)).encode("new_secure_pass");
        verify(userRepository, times(1)).save(existingUser);
    }

    @Test
    void updateUser_UserNotFound_ShouldThrowRuntimeException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            userService.updateUser(99L, new User());
        }, "User not found");

        verify(userRepository, never()).save(any());
    }

    @Test
    void reindexUserIds_ShouldReindexUsersAndResetSequence() {
        User u1 = User.builder()
                .id(10L)
                .username("u1")
                .build();
        User u2 = User.builder()
                .id(50L)
                .username("u2")
                .build();
        User u3 = User.builder()
                .id(100L)
                .username("u3")
                .build();
        List<User> initialUsers = Arrays.asList(u1, u2, u3);

        when(userRepository.findAllOrderByIdAsc()).thenReturn(initialUsers);
        userService.reindexUserIds();

        verify(userRepository, times(1)).updateUserId(10L, -1L);
        verify(userRepository, times(1)).updateUserId(50L, -2L);
        verify(userRepository, times(1)).updateUserId(100L, -3L);

        List<User> tempUsers = Arrays.asList(
                User.builder().id(-1L).username("u1").build(),
                User.builder().id(-2L).username("u2").build(),
                User.builder().id(-3L).username("u3").build()
        );
        when(userRepository.findAllOrderByIdAsc()).thenReturn(initialUsers, tempUsers);
        userService.reindexUserIds();

        verify(userRepository, times(1)).updateUserId(-1L, 1L);
        verify(userRepository, times(1)).updateUserId(-2L, 2L);
        verify(userRepository, times(1)).updateUserId(-3L, 3L);

        verify(jdbcTemplate, times(2)).execute(anyString());

        verify(entityManager, never()).clear();
    }
}