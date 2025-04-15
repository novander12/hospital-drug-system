package com.example.hospital.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "system_settings")
public class SystemSetting {
    
    @Id
    @Column(name = "setting_key", nullable = false, length = 100)
    private String settingKey;
    
    @Column(name = "setting_value", nullable = false, length = 500)
    private String settingValue;
    
    @Column(name = "description", length = 255)
    private String description;
    
    // 构造函数
    public SystemSetting() {
    }
    
    public SystemSetting(String settingKey, String settingValue, String description) {
        this.settingKey = settingKey;
        this.settingValue = settingValue;
        this.description = description;
    }
    
    // Getters and Setters
    public String getSettingKey() {
        return settingKey;
    }
    
    public void setSettingKey(String settingKey) {
        this.settingKey = settingKey;
    }
    
    public String getSettingValue() {
        return settingValue;
    }
    
    public void setSettingValue(String settingValue) {
        this.settingValue = settingValue;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    @Override
    public String toString() {
        return "SystemSetting{" +
                "settingKey='" + settingKey + '\'' +
                ", settingValue='" + settingValue + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
} 