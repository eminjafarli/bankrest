package com.project.bankrest.Services;

import com.project.bankrest.DTO.SignupRequest;
import com.project.bankrest.Entity.Role;
import com.project.bankrest.Entity.User;
import com.project.bankrest.Repository.UserRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final EntityManager entityManager;

    public List<User> getAllUsers() {
        entityManager.clear();
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
        reindexUserIds();
    }

    @Transactional
    public void reindexUserIds() {
        List<User> users = userRepository.findAllOrderByIdAsc();

        long tempId = -1;
        for (User user : users) {
            userRepository.updateUserId(user.getId(), tempId);
            tempId--;
        }

        List<User> tempUsers = userRepository.findAllOrderByIdAsc();

        long newId = 1;
        for (User user : tempUsers) {
            userRepository.updateUserId(user.getId(), newId);
            newId++;
        }

        resetSequence();
    }
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    private void resetSequence() {
        String sql = "SELECT setval(pg_get_serial_sequence('users', 'id'), COALESCE((SELECT MAX(id) FROM users), 1), true)";
        jdbcTemplate.execute(sql);
    }

    public User updateUser(Long id, User updatedUser) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setName(updatedUser.getName());

        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }

        if (updatedUser.getRole() != null) {
            existingUser.setRole(updatedUser.getRole());
        }

        return userRepository.save(existingUser);
    }


    public void register(SignupRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists.");
        }
        else if(request.getPassword().length() < 8){
            throw new IllegalArgumentException("Password must contain at least 8 letters.");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(Role.USER)
                .build();

        userRepository.save(user);
    }
    public Page<User> getUsers(String search, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        if (search != null && !search.isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCaseOrNameContainingIgnoreCase(search, search, pageable);
        }
        return userRepository.findAll(pageable);
    }

    public User addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists.");
        }

        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }

        return userRepository.save(user);
    }
}