package com.example.hospital.service;

import com.example.hospital.model.SystemSetting;
import com.example.hospital.repository.SystemSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class SystemSettingService {

    @Autowired
    private SystemSettingRepository systemSettingRepository;

    public List<SystemSetting> getAllSettings() {
        return systemSettingRepository.findAllByOrderBySettingKeyAsc();
    }

    public SystemSetting getSetting(String key) {
        return systemSettingRepository.findBySettingKey(key);
    }

    public String getSettingValue(String key, String defaultValue) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key);
        return setting != null ? setting.getSettingValue() : defaultValue;
    }

    public SystemSetting saveSetting(SystemSetting setting) {
        return systemSettingRepository.save(setting);
    }

    public void deleteSetting(String key) {
        systemSettingRepository.deleteBySettingKey(key);
    }

    public List<SystemSetting> getSettingsByPrefix(String prefix) {
        return systemSettingRepository.findBySettingKeyStartingWithOrderBySettingKey(prefix);
    }

    public String getValue(String key) {
        return getSettingValue(key, null);
    }

    public SystemSetting saveSetting(String key, String value, String description) {
        SystemSetting setting = new SystemSetting();
        setting.setSettingKey(key);
        setting.setSettingValue(value);
        setting.setDescription(description);
        return saveSetting(setting);
    }

    public Map<String, String> getSettingsAsMap() {
        List<SystemSetting> settings = getAllSettings();
        Map<String, String> map = settings.stream()
            .collect(Collectors.toMap(
                SystemSetting::getSettingKey,
                SystemSetting::getSettingValue
            ));
        return map;
    }

    public Map<String, String> getSettingsAsMap(String prefix) {
        List<SystemSetting> settings = getSettingsByPrefix(prefix);
        Map<String, String> map = settings.stream()
            .collect(Collectors.toMap(
                SystemSetting::getSettingKey,
                SystemSetting::getSettingValue
            ));
        return map;
    }
} 