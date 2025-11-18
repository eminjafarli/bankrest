package com.project.bankrest;

import com.project.bankrest.Controller.UserController;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Services.UserService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private User createMockUser(Long id, String name) {
        User user = new User();
        user.setId(id);
        user.setName(name);
        return user;
    }

    @Test
    void testGetUsersWithPagination() {
        String search = "admin";
        int page = 0;
        int size = 10;
        User user1 = createMockUser(1L, "Admin");
        Page<User> mockPage = new PageImpl<>(List.of(user1), PageRequest.of(page, size), 1);

        when(userService.getUsers(eq(search), eq(page), eq(size))).thenReturn(mockPage);

        ResponseEntity<?> response = userController.getUsers(search, page, size);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(mockPage, response.getBody());
        verify(userService, times(1)).getUsers(search, page, size);
    }

    @Test
    void testGetAllUsersForAdmin() {
        User user1 = createMockUser(1L, "UserA");
        User user2 = createMockUser(2L, "UserB");
        List<User> allUsers = List.of(user1, user2);

        when(userService.getAllUsers()).thenReturn(allUsers);

        List<User> response = userController.getAllUsersForAdmin();

        assertNotNull(response);
        assertEquals(2, response.size());
        assertEquals("UserA", response.get(0).getName());
        verify(userService, times(1)).getAllUsers();
    }

    @Test
    void testUpdateUser() {
        Long userId = 3L;
        User requestUser = createMockUser(userId, "NewName");
        User updatedUser = createMockUser(userId, "NewName");

        when(userService.updateUser(eq(userId), any(User.class))).thenReturn(updatedUser);

        ResponseEntity<User> response = userController.updateUser(userId, requestUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("NewName", response.getBody().getName());
        verify(userService, times(1)).updateUser(userId, requestUser);
    }

    @Test
    void testDeleteUser() {
        Long userId = 7L;

        doNothing().when(userService).deleteUser(userId);

        ResponseEntity<Void> response = userController.deleteUser(userId);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userService, times(1)).deleteUser(userId);
    }

    @Test
    void testAddUser() {
        User requestUser = createMockUser(null, "Newbie");
        requestUser.setPassword("securepass");

        User savedUser = createMockUser(10L, "Newbie");

        when(userService.addUser(any(User.class))).thenReturn(savedUser);

        ResponseEntity<User> response = userController.addUser(requestUser);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(10L, response.getBody().getId());
        assertEquals("Newbie", response.getBody().getName());
        verify(userService, times(1)).addUser(requestUser);
    }

    @Test
    void testGetUserById() {
        Long userId = 5L;
        User mockUser = createMockUser(userId, "Finder");

        when(userService.getUserById(userId)).thenReturn(mockUser);

        ResponseEntity<User> response = userController.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(userId, response.getBody().getId());
        verify(userService, times(1)).getUserById(userId);
    }
}
