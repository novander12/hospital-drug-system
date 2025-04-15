package com.example.hospital.controller;

import com.example.hospital.model.User;
import com.example.hospital.repository.UserRepository;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private UserService userService;
    
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/password")
    public Map<String, Object> testPassword(@RequestParam(defaultValue = "admin123") String rawPassword) {
        Map<String, Object> result = new HashMap<>();
        
        // 加密密码
        String encodedPassword = passwordEncoder.encode(rawPassword);
        
        // 检查是否匹配
        boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);
        
        // 固定的加密密码用于测试
        String fixedEncodedPassword = "$2a$10$QUARTc5q2yrBL3nEKRH1WOqsW0.UNzfO0n/2GbkxGKbVJllSqzJna";
        boolean matchesFixed = passwordEncoder.matches(rawPassword, fixedEncodedPassword);
        
        result.put("rawPassword", rawPassword);
        result.put("encodedPassword", encodedPassword);
        result.put("passwordLength", encodedPassword.length());
        result.put("matches", matches);
        result.put("fixedEncodedPassword", fixedEncodedPassword);
        result.put("matchesFixed", matchesFixed);
        
        return result;
    }
    
    @GetMapping("/reset-admin")
    public Map<String, Object> resetAdminPassword() {
        Map<String, Object> result = new HashMap<>();
        
        User admin = userService.findByUsername("admin");
        if (admin != null) {
            // 直接设置为固定的加密密码
            String originalPassword = "admin123";
            String encodedPassword = passwordEncoder.encode(originalPassword);
            admin.setPassword(encodedPassword);
            userRepository.save(admin);
            
            result.put("status", "success");
            result.put("message", "管理员密码已重置为 admin123");
            result.put("encodedPassword", encodedPassword);
            result.put("passwordLength", encodedPassword.length());
            result.put("matches", passwordEncoder.matches(originalPassword, encodedPassword));
        } else {
            result.put("status", "error");
            result.put("message", "找不到管理员用户");
        }
        
        return result;
    }
    
    @PostMapping("/login")
    public Map<String, Object> testLogin(@RequestBody Map<String, String> loginRequest) {
        Map<String, Object> result = new HashMap<>();
        
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        result.put("username", username);
        
        User user = userRepository.findByUsername(username);
        if (user != null) {
            result.put("userFound", true);
            result.put("encodedPassword", user.getPassword());
            result.put("passwordLength", user.getPassword().length());
            
            boolean matches = passwordEncoder.matches(password, user.getPassword());
            result.put("passwordMatches", matches);
            
            if (matches) {
                result.put("status", "success");
                result.put("message", "登录成功");
                
                Map<String, Object> userInfo = new HashMap<>();
                userInfo.put("id", user.getId());
                userInfo.put("username", user.getUsername());
                userInfo.put("role", user.getRole());
                result.put("user", userInfo);
            } else {
                result.put("status", "error");
                result.put("message", "密码不匹配");
            }
        } else {
            result.put("userFound", false);
            result.put("status", "error");
            result.put("message", "用户不存在");
        }
        
        return result;
    }
} 