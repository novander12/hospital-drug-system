package com.example.hospital.controller;

import com.example.hospital.model.SystemSetting;
import com.example.hospital.service.SystemSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/settings")
public class SystemSettingController {

    @Autowired
    private SystemSettingService systemSettingService;

    /**
     * 获取所有系统设置
     * @return 系统设置列表
     */
    @GetMapping
    public ResponseEntity<List<SystemSetting>> getAllSettings() {
        return ResponseEntity.ok(systemSettingService.getAllSettings());
    }

    /**
     * 根据键获取系统设置
     * @param key 设置键
     * @return 系统设置
     */
    @GetMapping("/{key}")
    public ResponseEntity<SystemSetting> getSettingByKey(@PathVariable String key) {
        SystemSetting setting = systemSettingService.getSetting(key);
        return setting != null 
                ? ResponseEntity.ok(setting) 
                : ResponseEntity.notFound().build();
    }

    /**
     * 更新或创建系统设置列表
     * @param settings 系统设置列表
     * @return 更新后的系统设置列表
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SystemSetting>> saveSettings(@RequestBody List<SystemSetting> settings) {
        List<SystemSetting> savedSettings = settings.stream()
                .map(systemSettingService::saveSetting)
                .collect(Collectors.toList());
        return ResponseEntity.ok(savedSettings);
    }

    /**
     * 更新单个系统设置
     * @param key 设置键
     * @param setting 系统设置
     * @return 更新后的系统设置
     */
    @PutMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SystemSetting> updateSetting(@PathVariable String key, @RequestBody SystemSetting setting) {
        if (!key.equals(setting.getSettingKey())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(systemSettingService.saveSetting(setting));
    }

    /**
     * 删除系统设置
     * @param key 设置键
     * @return 无内容响应
     */
    @DeleteMapping("/{key}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSetting(@PathVariable String key) {
        systemSettingService.deleteSetting(key);
        return ResponseEntity.noContent().build();
    }
}