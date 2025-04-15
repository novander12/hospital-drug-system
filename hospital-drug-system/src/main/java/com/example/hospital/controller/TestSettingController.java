package com.example.hospital.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/test-settings")
public class TestSettingController {

    @GetMapping
    public Map<String, Object> getTest() {
        Map<String, Object> result = new HashMap<>();
        result.put("message", "测试设置控制器正常工作");
        result.put("status", "success");
        return result;
    }
} 