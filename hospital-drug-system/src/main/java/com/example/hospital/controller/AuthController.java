package com.example.hospital.controller;

import com.example.hospital.model.Role;
import com.example.hospital.model.User;
import com.example.hospital.service.UserService;
import com.example.hospital.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器，处理用户登录等认证相关操作
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private UserService userService;
    
    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 用户登录接口
     * @param loginRequest 包含用户名和密码的请求体
     * @return 登录结果
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        // 验证请求参数
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名和密码不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 验证用户身份
        User user = userService.authenticate(username, password);
        
        if (user != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "登录成功");
            
            // 创建用户信息(不包含密码)
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("id", user.getId());
            userInfo.put("username", user.getUsername());
            userInfo.put("role", user.getRole() != null ? user.getRole().name() : null);
            response.put("user", userInfo);
            
            // 生成JWT令牌
            String token = jwtUtil.generateToken(user);
            response.put("token", token);
            
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名或密码错误");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
    }
    
    /**
     * 检查用户权限
     * @param username 用户名
     * @return 权限信息
     */
    @GetMapping("/check-permission")
    public ResponseEntity<?> checkPermission(@RequestParam String username) {
        Map<String, Object> response = new HashMap<>();
        
        boolean isAdmin = userService.isAdmin(username);
        response.put("isAdmin", isAdmin);
        
        if (isAdmin) {
            response.put("permissions", "full");
            response.put("message", "管理员拥有所有操作权限");
        } else {
            response.put("permissions", "read-only");
            response.put("message", "普通用户仅有查看权限");
        }
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 用户注册接口
     * @param registerRequest 包含用户注册信息的请求体
     * @return 注册结果
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> registerRequest) {
        String username = registerRequest.get("username");
        String password = registerRequest.get("password");
        String roleStr = registerRequest.get("role");
        
        // 验证请求参数
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名和密码不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        // 检查用户名是否已存在
        if (userService.findByUsername(username) != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }
        
        // Convert role string to Role enum safely
        Role userRole = Role.USER; // Default role
        if (roleStr != null && !roleStr.isEmpty()) {
            try {
                userRole = Role.valueOf(roleStr.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid role provided during registration: " + roleStr + ". Defaulting to USER.");
            }
        }

        // Call the correct createUser method
        User createdUser = userService.createUser(username, password, userRole);
        
        // 创建响应
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "注册成功");
        
        // 创建用户信息(不包含密码, role as string)
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", createdUser.getId());
        userInfo.put("username", createdUser.getUsername());
        userInfo.put("role", createdUser.getRole() != null ? createdUser.getRole().name() : null);
        response.put("user", userInfo);
        
        // 生成JWT令牌
        String token = jwtUtil.generateToken(createdUser);
        response.put("token", token);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
} 