package com.example.hospital.repository;

import com.example.hospital.model.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SystemSettingRepository extends JpaRepository<SystemSetting, Long> {
    
    /**
     * 根据键查找设置
     * 
     * @param settingKey 设置键
     * @return 系统设置
     */
    SystemSetting findBySettingKey(String settingKey);
    
    /**
     * 查找以指定前缀开头的所有设置
     * 
     * @param prefix 前缀
     * @return 符合条件的设置列表
     */
    List<SystemSetting> findBySettingKeyStartingWithOrderBySettingKey(String prefix);
    
    /**
     * 按键排序获取所有设置
     * 
     * @return 按键排序的设置列表
     */
    List<SystemSetting> findAllByOrderBySettingKey();
    
    /**
     * 按键升序排序获取所有设置
     * 
     * @return 按键升序排序的设置列表
     */
    List<SystemSetting> findAllByOrderBySettingKeyAsc();
    
    /**
     * 删除指定键的设置
     * 
     * @param settingKey
     */
    void deleteBySettingKey(String settingKey);
} 