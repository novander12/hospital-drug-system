package com.example.hospital.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AdminPasswordResetDTO {

    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 20, message = "新密码长度必须在 6 到 20 个字符之间")
    private String newPassword;

    // Getters and Setters
    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }
} 