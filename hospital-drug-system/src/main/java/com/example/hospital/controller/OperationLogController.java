package com.example.hospital.controller;

import com.example.hospital.model.OperationLog;
import com.example.hospital.service.OperationLogService;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logs")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @Autowired
    private UserService userService; // UserService is needed for isAdmin check

    // --- Helper Methods ---

    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null; // Indicate no authenticated user
        }
        return authentication.getName();
    }

    private boolean isCurrentUserAdmin() {
        String username = getCurrentUsername();
        // Ensure userService is injected before calling isAdmin
        return username != null && userService != null && userService.isAdmin(username);
    }

    private Pageable createPageable(int page, int size, String[] sort) throws IllegalArgumentException {
         try {
             if (sort == null || sort.length < 2 || sort[0].isEmpty() || sort[1].isEmpty()) {
                 // Default sort if parameter is incomplete or missing
                 sort = new String[]{"timestamp", "desc"};
             }
             Sort.Direction direction = Sort.Direction.fromString(sort[1]);
             Sort sortOrder = Sort.by(direction, sort[0]);
             // Basic validation for page and size
             if (page < 0) page = 0;
             if (size < 1) size = 1; // Avoid division by zero or invalid size
             if (size > 100) size = 100; // Optional: Limit max page size

             return PageRequest.of(page, size, sortOrder);
         } catch (IllegalArgumentException e) {
             throw new IllegalArgumentException("无效的排序参数: " + String.join(",", sort) + " - " + e.getMessage(), e);
         }
     }

    // --- API Endpoints ---

    /**
     * 获取所有操作日志 (带筛选和分页) - 仅管理员
     */
    @GetMapping
    public ResponseEntity<?> getAllLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String type,    // 操作类型 (action)
            @RequestParam(required = false) String username,// 操作用户 (筛选用)
            @RequestParam(required = false) String result,  // 操作结果
            @RequestParam(required = false) String keyword, // 关键字
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort // e.g., sort=timestamp,desc (Optional now)
    ) {
        if (!isCurrentUserAdmin()) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "权限不足，仅管理员可查看所有操作日志");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
        }

        try {
            Pageable pageable = createPageable(page, size, sort);
            Page<OperationLog> logPage = operationLogService.findLogsFiltered(
                    username, type, result, startDate, endDate, keyword, pageable
            );
            return ResponseEntity.ok(logPage);
        } catch (IllegalArgumentException e) {
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("status", "error");
             errorResponse.put("message", "请求参数错误: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) { // Catch broader exceptions during service call
             // Log the exception e
             System.err.println("Error fetching all logs: " + e.getMessage());
             e.printStackTrace();
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("status", "error");
             errorResponse.put("message", "服务器内部错误，请联系管理员");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取当前用户的操作日志 (带筛选和分页)
     */
    @GetMapping("/my")
    public ResponseEntity<?> getMyLogs(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String result,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String[] sort
    ) {
        String currentUsername = getCurrentUsername();
        if (currentUsername == null) {
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("status", "error");
             errorResponse.put("message", "用户未认证");
             return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
        }

        try {
             Pageable pageable = createPageable(page, size, sort);
             Page<OperationLog> logPage = operationLogService.findCurrentUserLogsFiltered(
                     currentUsername, type, result, startDate, endDate, keyword, pageable
             );
             return ResponseEntity.ok(logPage);
        } catch (IllegalArgumentException e) {
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("status", "error");
             errorResponse.put("message", "请求参数错误: " + e.getMessage());
             return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) { // Catch broader exceptions
             // Log the exception e
             System.err.println("Error fetching current user logs: " + e.getMessage());
             e.printStackTrace();
             Map<String, Object> errorResponse = new HashMap<>();
             errorResponse.put("status", "error");
             errorResponse.put("message", "服务器内部错误，请联系管理员");
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * 获取所有不同的操作类型列表
     */
    @GetMapping("/types")
    public ResponseEntity<List<String>> getActionTypes() {
        try {
            List<String> types = operationLogService.getDistinctActionTypes();
            return ResponseEntity.ok(types);
        } catch (Exception e) {
            // Log the exception e
            System.err.println("Error fetching action types: " + e.getMessage());
            e.printStackTrace();
            // Return an empty list or an error response
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    // Removed old endpoints like /drug/{id} as they are covered by filtering
}