package com.example.hospital.controller;

import com.example.hospital.dto.UserDTO;
import com.example.hospital.model.LoginHistory;
import com.example.hospital.model.Role;
import com.example.hospital.model.User;
import com.example.hospital.service.UserService;
import com.example.hospital.dto.PasswordChangeRequestDTO;
import com.example.hospital.dto.AdminPasswordResetDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 获取所有用户 (仅管理员可访问, 返回 UserDTO 列表)
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> userDTOs = userService.findAllUserDTOs();
        return ResponseEntity.ok(userDTOs);
    }

    /**
     * 创建新用户 (仅管理员可访问, 返回包含 UserDTO 的 Map)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createUser(@RequestBody User user) {
        if (userService.userExists(user.getUsername())) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "用户名已存在");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        }

        User savedUser = userService.createUser(user.getUsername(), user.getPassword(), user.getRole());
        UserDTO savedUserDTO = userService.convertToUserDTO(savedUser);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "用户创建成功");
        response.put("user", savedUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 更新用户角色 (仅管理员可访问, 返回包含 UserDTO 的 Map)
     */
    @PutMapping("/{id}/role")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id, @RequestBody Map<String, String> payload) {
        String roleStr = payload.get("role");
        if (roleStr == null) {
             Map<String, Object> response = new HashMap<>();
             response.put("status", "error");
             response.put("message", "请求体中缺少 'role' 字段");
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        Role newRole;
        try {
             newRole = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
             Map<String, Object> response = new HashMap<>();
             response.put("status", "error");
             response.put("message", "无效的角色: " + roleStr);
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        try {
            User updatedUser = userService.updateUserRole(id, newRole);
            UserDTO updatedUserDTO = userService.convertToUserDTO(updatedUser);
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "用户角色更新成功");
            response.put("user", updatedUserDTO);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response); 
        }
    }

    /**
     * 删除用户 (仅管理员可访问)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
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
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * 修改当前登录用户的密码
     */
    @PutMapping("/change-password")
    public ResponseEntity<?> changeCurrentUserPassword(
            @Valid @RequestBody PasswordChangeRequestDTO passwordRequest,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        if (userDetails == null) {
             Map<String, Object> response = new HashMap<>();
             response.put("status", "error");
             response.put("message", "用户未认证");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        String username = userDetails.getUsername();
        try {
            userService.changePassword(username, passwordRequest);
             Map<String, Object> response = new HashMap<>();
             response.put("status", "success");
             response.put("message", "密码修改成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
             Map<String, Object> response = new HashMap<>();
             response.put("status", "error");
             response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
             Map<String, Object> response = new HashMap<>();
             response.put("status", "error");
             response.put("message", "修改密码失败，发生内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    /**
     * 管理员重置指定用户的密码
     */
    @PutMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetUserPasswordByAdmin(
            @PathVariable Long id,
            @Valid @RequestBody AdminPasswordResetDTO passwordResetRequest
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication != null ? authentication.getName() : null;
        User targetUser = userService.findById(id);
        if (targetUser != null && targetUser.getUsername().equals(currentUsername)) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "管理员请使用'用户设置'修改自己的密码");
            return ResponseEntity.badRequest().body(response);
        }
        
        try {
            userService.resetPasswordByAdmin(id, passwordResetRequest.getNewPassword());
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "用户密码重置成功");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "密码重置失败，发生内部错误");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
} 