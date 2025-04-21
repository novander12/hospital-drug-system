package com.example.hospital.controller;

import com.example.hospital.model.LoginHistory;
import com.example.hospital.model.User;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    private static final Map<String, User> USERS = new HashMap<>();
    private static final List<LoginHistory> LOGIN_HISTORY = new ArrayList<>();
    
    static {
        // 添加测试用户数据
        User admin = new User();
        admin.setUsername("admin");
        admin.setRealName("管理员");
        admin.setRole("ADMIN");
        admin.setDepartment("管理部");
        admin.setEmail("admin@hospital.com");
        admin.setPhone("13800138000");
        USERS.put(admin.getUsername(), admin);
        
        User nurse = new User();
        nurse.setUsername("nurse");
        nurse.setRealName("张护士");
        nurse.setRole("NURSE");
        nurse.setDepartment("内科");
        nurse.setEmail("nurse@hospital.com");
        nurse.setPhone("13800138001");
        USERS.put(nurse.getUsername(), nurse);
        
        User pharmacist = new User();
        pharmacist.setUsername("pharmacist");
        pharmacist.setRealName("李药师");
        pharmacist.setRole("PHARMACIST");
        pharmacist.setDepartment("药剂科");
        pharmacist.setEmail("pharmacist@hospital.com");
        pharmacist.setPhone("13800138002");
        USERS.put(pharmacist.getUsername(), pharmacist);
        
        // 添加登录历史测试数据
        LoginHistory h1 = new LoginHistory();
        h1.setId(1L);
        h1.setUsername("admin");
        h1.setLoginTime(LocalDateTime.now().minusDays(1));
        h1.setIpAddress("192.168.1.100");
        h1.setDevice("Chrome 95.0.4638.69 / Windows 10");
        h1.setStatus("SUCCESS");
        LOGIN_HISTORY.add(h1);
        
        LoginHistory h2 = new LoginHistory();
        h2.setId(2L);
        h2.setUsername("nurse");
        h2.setLoginTime(LocalDateTime.now().minusHours(5));
        h2.setIpAddress("192.168.1.101");
        h2.setDevice("Firefox 94.0 / MacOS");
        h2.setStatus("SUCCESS");
        LOGIN_HISTORY.add(h2);
    }

    /**
     * 检查当前用户是否有管理员权限
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        return username != null && userService.isAdmin(username);
    }

    /**
     * 获取所有用户 (仅管理员可访问)
     */
    @GetMapping
    public ResponseEntity<?> getAllUsers() {
        // 检查权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可查看用户列表");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        List<User> users = userService.findAllUsers();
        // 移除密码字段
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    /**
     * 创建新用户 (仅管理员可访问)
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody User user) {
        // 检查权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可创建用户");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // 检查用户名是否已存在
        if (userService.userExists(user.getUsername())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User savedUser = userService.createUser(user.getUsername(), user.getPassword(), user.getRole());
        // 移除密码字段
        savedUser.setPassword(null);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "用户创建成功");
        response.put("user", savedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 更新用户角色 (仅管理员可访问)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody User user) {
        // 检查权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可更新用户");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        try {
            User updatedUser = userService.updateUserRole(id, user.getRole());
            // 移除密码字段
            updatedUser.setPassword(null);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "用户更新成功");
            response.put("user", updatedUser);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 删除用户 (仅管理员可访问)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        // 检查权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可删除用户");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }

        // 检查是否为当前用户，不允许删除自己
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        User userToDelete = userService.findById(id);
        
        if (userToDelete == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        if (userToDelete.getUsername().equals(currentUsername)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "不能删除当前登录的用户");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            userService.deleteUser(id);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "用户删除成功");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        System.out.println("尝试登录: 用户名=" + username + ", 密码长度=" + (password != null ? password.length() : 0));
        
        User user = userService.authenticate(username, password);
        
        if (user != null) {
            System.out.println("登录成功: " + user);
            Map<String, Object> response = createSuccessResponse("登录成功", user);
            return ResponseEntity.ok(response);
        } else {
            System.out.println("登录失败: 用户名或密码错误");
            return ResponseEntity.badRequest().body(createErrorResponse("用户名或密码错误"));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody User user) {
        // 检查用户名是否已存在
        if (userService.findByUsername(user.getUsername()) != null) {
            return ResponseEntity.badRequest().body(createErrorResponse("用户名已存在"));
        }
        
        // 使用Service层创建用户
        User savedUser = userService.createUser(user);
        
        Map<String, Object> response = createSuccessResponse("注册成功", savedUser);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/check")
    public ResponseEntity<?> checkUser(@RequestParam String username) {
        User user = userService.findByUsername(username);
        if (user != null) {
            Map<String, Object> userData = new HashMap<>();
            userData.put("id", user.getId());
            userData.put("username", user.getUsername());
            userData.put("role", user.getRole());
            userData.put("passwordLength", user.getPassword().length());
            
            return ResponseEntity.ok(userData);
        } else {
            return ResponseEntity.badRequest().body(createErrorResponse("用户不存在"));
        }
    }
    
    /**
     * 创建成功响应
     */
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        response.put("data", data);
        return response;
    }
    
    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfile() {
        // 在实际应用中，应该从认证上下文中获取当前用户
        // 这里简单返回第一个用户作为演示
        String username = "admin"; // 模拟当前登录用户
        User user = USERS.get(username);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/profile")
    public ResponseEntity<User> updateUserProfile(@RequestBody User userUpdate) {
        // 在实际应用中，应该从认证上下文中获取当前用户
        String username = "admin"; // 模拟当前登录用户
        User user = USERS.get(username);
        
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // 只允许更新部分字段
        if (userUpdate.getRealName() != null) {
            user.setRealName(userUpdate.getRealName());
        }
        if (userUpdate.getDepartment() != null) {
            user.setDepartment(userUpdate.getDepartment());
        }
        if (userUpdate.getEmail() != null) {
            user.setEmail(userUpdate.getEmail());
        }
        if (userUpdate.getPhone() != null) {
            user.setPhone(userUpdate.getPhone());
        }
        
        return ResponseEntity.ok(user);
    }
    
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, String> passwordData) {
        // 在实际应用中，应该从认证上下文中获取当前用户
        String username = "admin"; // 模拟当前登录用户
        
        String currentPassword = passwordData.get("currentPassword");
        String newPassword = passwordData.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("当前密码和新密码不能为空");
        }
        
        // 在实际应用中，应该验证当前密码是否正确
        // 并对新密码进行安全哈希处理
        
        return ResponseEntity.ok().body("密码修改成功");
    }
    
    @GetMapping("/login-history")
    public ResponseEntity<List<LoginHistory>> getLoginHistory() {
        // 在实际应用中，应该根据当前用户过滤历史记录
        return ResponseEntity.ok(LOGIN_HISTORY);
    }
} 