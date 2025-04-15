package com.example.hospital.controller;

import com.example.hospital.model.OperationLog;
import com.example.hospital.service.OperationLogService;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;
    
    @Autowired
    private UserService userService;
    
    /**
     * 检查当前用户是否有管理员权限
     */
    private boolean isCurrentUserAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication != null ? authentication.getName() : null;
        return username != null && userService.isAdmin(username);
    }
    
    /**
     * 获取所有操作日志
     */
    @GetMapping
    public ResponseEntity<?> getAllLogs() {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可查看操作日志");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        List<OperationLog> logs = operationLogService.findAllLogs();
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取当前用户的操作日志
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyLogs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        List<OperationLog> logs = operationLogService.findLogsByUsername(username);
        return ResponseEntity.ok(logs);
    }
    
    /**
     * 获取指定药品的操作日志
     */
    @GetMapping("/drug/{id}")
    public ResponseEntity<?> getDrugLogs(@PathVariable Long id) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可查看药品操作日志");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        List<OperationLog> logs = operationLogService.findLogsByDrugId(id);
        return ResponseEntity.ok(logs);
    }
} 