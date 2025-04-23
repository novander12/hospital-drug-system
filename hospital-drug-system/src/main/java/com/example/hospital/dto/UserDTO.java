package com.example.hospital.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 数据传输对象，用于向前端传递用户信息（不含密码）
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;
    private String username;
    private String role; // Role as String
    private String realName;
    private String department;
    private String email;
    private String phone;

    // 可以根据需要添加或移除字段
} 