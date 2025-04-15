package com.example.hospital;

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
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("testuser");
        testUser.setPassword("password123");
        
        User savedUser = userService.createUser(testUser);
        
        // 验证用户已保存
        assertNotNull(savedUser.getId());
        assertEquals("testuser", savedUser.getUsername());
        assertEquals("USER", savedUser.getRole()); // 默认角色
        assertTrue(passwordEncoder.matches("password123", savedUser.getPassword())); // 验证密码被加密
    }
    
    @Test
    public void testAuthenticate() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("authuser");
        testUser.setPassword("password123");
        
        userService.createUser(testUser);
        
        // 测试正确密码
        User authenticatedUser = userService.authenticate("authuser", "password123");
        assertNotNull(authenticatedUser);
        assertEquals("authuser", authenticatedUser.getUsername());
        
        // 测试错误密码
        User failedAuth = userService.authenticate("authuser", "wrongpassword");
        assertNull(failedAuth);
        
        // 测试不存在的用户
        User nonExistentUser = userService.authenticate("nonexistent", "anypassword");
        assertNull(nonExistentUser);
    }
    
    @Test
    public void testFindByUsername() {
        // 创建测试用户
        User testUser = new User();
        testUser.setUsername("finduser");
        testUser.setPassword("password123");
        
        userService.createUser(testUser);
        
        // 测试查找存在的用户
        User foundUser = userService.findByUsername("finduser");
        assertNotNull(foundUser);
        assertEquals("finduser", foundUser.getUsername());
        
        // 测试查找不存在的用户
        User notFoundUser = userService.findByUsername("nonexistent");
        assertNull(notFoundUser);
    }
    
    @Test
    public void testIsAdmin() {
        // 创建管理员用户
        User adminUser = new User();
        adminUser.setUsername("adminuser");
        adminUser.setPassword("password123");
        adminUser.setRole("ADMIN");
        
        userService.createUser(adminUser);
        
        // 创建普通用户
        User normalUser = new User();
        normalUser.setUsername("normaluser");
        normalUser.setPassword("password123");
        normalUser.setRole("USER");
        
        userService.createUser(normalUser);
        
        // 测试管理员用户
        assertTrue(userService.isAdmin("adminuser"));
        
        // 测试普通用户
        assertFalse(userService.isAdmin("normaluser"));
        
        // 测试不存在的用户
        assertFalse(userService.isAdmin("nonexistent"));
    }
} 