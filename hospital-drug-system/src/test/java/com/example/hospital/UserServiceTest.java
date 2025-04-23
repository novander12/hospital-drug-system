package com.example.hospital;

import com.example.hospital.model.Role;
import com.example.hospital.model.User;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class UserServiceTest {

    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Test
    public void testCreateUser() {
        String username = "testuser";
        String rawPassword = "password123";
        Role role = Role.USER;
        
        User savedUser = userService.createUser(username, rawPassword, role);
        
        assertNotNull(savedUser.getId());
        assertEquals(username, savedUser.getUsername());
        assertEquals(role, savedUser.getRole());
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }
    
    @Test
    public void testCreateAdminUser() {
        String username = "testadmin";
        String rawPassword = "adminpass";
        Role role = Role.ADMIN;
        
        User savedAdmin = userService.createUser(username, rawPassword, role);
        
        assertNotNull(savedAdmin.getId());
        assertEquals(username, savedAdmin.getUsername());
        assertEquals(role, savedAdmin.getRole());
        assertTrue(passwordEncoder.matches(rawPassword, savedAdmin.getPassword()));
    }
    
    @Test
    public void testAuthenticate() {
        String username = "authuser";
        String rawPassword = "password123";
        Role role = Role.USER;
        
        userService.createUser(username, rawPassword, role);
        
        User authenticatedUser = userService.authenticate(username, rawPassword);
        assertNotNull(authenticatedUser);
        assertEquals(username, authenticatedUser.getUsername());
        
        User failedAuth = userService.authenticate(username, "wrongpassword");
        assertNull(failedAuth);
        
        User nonExistentUser = userService.authenticate("nonexistent", "anypassword");
        assertNull(nonExistentUser);
    }
    
    @Test
    public void testFindByUsername() {
        String username = "finduser";
        String rawPassword = "password123";
        Role role = Role.USER;
        
        userService.createUser(username, rawPassword, role);
        
        User foundUser = userService.findByUsername(username);
        assertNotNull(foundUser);
        assertEquals(username, foundUser.getUsername());
        
        User notFoundUser = userService.findByUsername("nonexistent");
        assertNull(notFoundUser);
    }
    
    @Test
    public void testIsAdmin() {
        userService.createUser("adminuser", "password123", Role.ADMIN);
        
        userService.createUser("normaluser", "password123", Role.USER);
        
        User adminUser = userService.findByUsername("adminuser");
        User normalUser = userService.findByUsername("normaluser");
        
        assertEquals(Role.ADMIN, adminUser.getRole());
        assertEquals(Role.USER, normalUser.getRole());
    }
} 