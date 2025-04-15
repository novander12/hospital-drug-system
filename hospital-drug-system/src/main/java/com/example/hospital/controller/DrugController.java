package com.example.hospital.controller;

import com.example.hospital.model.Drug;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.service.OperationLogService;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import javax.validation.Valid;

import java.net.URI;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    @Autowired
    private DrugRepository drugRepository;
    
    @Autowired
    private UserService userService;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取当前认证用户的用户名
     */
    private String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : null;
    }

    /**
     * 检查当前用户是否有管理员权限
     */
    private boolean isCurrentUserAdmin() {
        String username = getCurrentUsername();
        return username != null && userService.isAdmin(username);
    }

    /**
     * 获取所有药品 (所有用户都可访问)
     */
    @GetMapping
    public ResponseEntity<List<Drug>> getAllDrugs() {
        List<Drug> drugs = drugRepository.findAll();
        return ResponseEntity.ok(drugs);
    }

    /**
     * 根据ID获取药品 (所有用户都可访问)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDrugById(@PathVariable Long id) {
        Optional<Drug> drug = drugRepository.findById(id);
        if (drug.isPresent()) {
            return ResponseEntity.ok(drug.get());
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "药品不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 创建新药品 (仅管理员可访问)
     */
    @PostMapping
    public ResponseEntity<?> createDrug(@Valid @RequestBody Drug drug) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可创建药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        // 添加基本验证
        if (drug.getStock() == null || drug.getStock() < 0) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "药品库存不能为负数");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
        
        Drug savedDrug = drugRepository.save(drug);
        
        // 记录操作日志
        operationLogService.logAddDrug(savedDrug);
        
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedDrug.getId())
                .toUri();
        
        return ResponseEntity.created(location).body(savedDrug);
    }

    /**
     * 批量创建药品 (仅管理员可访问)
     */
    @PostMapping("/batch")
    public ResponseEntity<?> createDrugs(@RequestBody List<Drug> drugs) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可批量创建药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        List<Drug> savedDrugs = drugRepository.saveAll(drugs);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDrugs);
    }

    /**
     * 更新药品信息 (仅管理员可访问)
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDrug(@PathVariable Long id, @RequestBody Drug drugDetails) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可更新药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        Optional<Drug> optionalDrug = drugRepository.findById(id);
        if (optionalDrug.isPresent()) {
            Drug drug = optionalDrug.get();
            Drug oldDrug = new Drug(); // 保存旧值用于日志
            oldDrug.setId(drug.getId());
            oldDrug.setName(drug.getName());
            oldDrug.setSpec(drug.getSpec());
            oldDrug.setStock(drug.getStock());
            oldDrug.setExpirationDate(drug.getExpirationDate());
            oldDrug.setSupplier(drug.getSupplier());
            oldDrug.setCategory(drug.getCategory());
            
            drug.setName(drugDetails.getName());
            drug.setSpec(drugDetails.getSpec());
            drug.setStock(drugDetails.getStock());
            drug.setExpirationDate(drugDetails.getExpirationDate());
            drug.setSupplier(drugDetails.getSupplier());
            drug.setCategory(drugDetails.getCategory());
            Drug updatedDrug = drugRepository.save(drug);
            
            // 记录操作日志
            operationLogService.logUpdateDrug(oldDrug, updatedDrug);
            
            return ResponseEntity.ok(updatedDrug);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "药品不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 局部更新药品信息 (仅管理员可访问)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<?> partialUpdateDrug(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可更新药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        Optional<Drug> optionalDrug = drugRepository.findById(id);
        if (optionalDrug.isPresent()) {
            Drug drug = optionalDrug.get();
            
            // 根据提供的字段进行更新
            if (updates.containsKey("name")) {
                drug.setName((String) updates.get("name"));
            }
            if (updates.containsKey("spec")) {
                drug.setSpec((String) updates.get("spec"));
            }
            if (updates.containsKey("stock")) {
                drug.setStock((Integer) updates.get("stock"));
            }
            if (updates.containsKey("supplier")) {
                drug.setSupplier((String) updates.get("supplier"));
            }
            if (updates.containsKey("category")) {
                drug.setCategory((String) updates.get("category"));
            }
            if (updates.containsKey("expirationDate")) {
                drug.setExpirationDate(java.time.LocalDate.parse((String) updates.get("expirationDate")));
            }
            
            Drug updatedDrug = drugRepository.save(drug);
            return ResponseEntity.ok(updatedDrug);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "药品不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }

    /**
     * 删除药品 (仅管理员可访问)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteDrug(@PathVariable Long id) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可删除药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        Optional<Drug> optionalDrug = drugRepository.findById(id);
        if (optionalDrug.isPresent()) {
            Drug drug = optionalDrug.get();
            
            // 记录操作日志
            operationLogService.logDeleteDrug(drug);
            
            drugRepository.deleteById(id);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "药品已删除");
            return ResponseEntity.ok(response);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "药品不存在");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
    }
    
    /**
     * 批量删除药品 (仅管理员可访问)
     */
    @DeleteMapping("/batch")
    public ResponseEntity<?> deleteDrugs(@RequestBody List<Long> ids) {
        // 检查用户权限
        if (!isCurrentUserAdmin()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "权限不足，仅管理员可批量删除药品");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
        }
        
        List<Drug> drugsToDelete = new ArrayList<>();
        
        ids.forEach(id -> {
            Optional<Drug> drug = drugRepository.findById(id);
            drug.ifPresent(drugsToDelete::add);
        });
        
        if (!drugsToDelete.isEmpty()) {
            // 记录操作日志
            operationLogService.logBatchDeleteDrugs(drugsToDelete);
            
            ids.forEach(id -> {
                if (drugRepository.existsById(id)) {
                    drugRepository.deleteById(id);
                }
            });
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "已批量删除药品");
        return ResponseEntity.ok(response);
    }

    /**
     * 根据药品名称进行模糊搜索 (所有用户都可访问)
     * @param name 药品名称关键词
     * @return 匹配的药品列表
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDrugsByName(@RequestParam String name) {
        if (name == null || name.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "搜索关键词不能为空");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        List<Drug> matchedDrugs = drugRepository.findByNameContainingIgnoreCase(name.trim());
        
        Map<String, Object> response = new HashMap<>();
        if (matchedDrugs.isEmpty()) {
            response.put("status", "info");
            response.put("message", "未找到匹配的药品");
        } else {
            response.put("status", "success");
            response.put("message", "找到" + matchedDrugs.size() + "条匹配记录");
        }
        response.put("drugs", matchedDrugs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取库存预警药品列表（库存低于阈值的药品）
     */
    @GetMapping("/low-stock")
    public ResponseEntity<?> getLowStockDrugs() {
        final int LOW_STOCK_THRESHOLD = 10; // 库存预警阈值
        List<Drug> lowStockDrugs = drugRepository.findByStockLessThan(LOW_STOCK_THRESHOLD);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", lowStockDrugs.size());
        response.put("message", "共找到" + lowStockDrugs.size() + "种库存不足的药品");
        response.put("data", lowStockDrugs);
        
        return ResponseEntity.ok(response);
    }

    /**
     * 获取即将过期的药品列表
     */
    @GetMapping("/expiring")
    public ResponseEntity<?> getExpiringDrugs(@RequestParam(value = "days", defaultValue = "30") int days) {
        LocalDate endDate = LocalDate.now().plusDays(days);
        List<Drug> expiringDrugs = drugRepository.findExpiringDrugs(endDate);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("count", expiringDrugs.size());
        response.put("drugs", expiringDrugs);
        return ResponseEntity.ok(response);
    }

    /**
     * 导出药品为CSV文件
     */
    @GetMapping("/export")
    public ResponseEntity<?> exportDrugsToCSV() {
        List<Drug> drugs = drugRepository.findAll();
        
        if (drugs.isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "没有可导出的药品数据");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        
        // 使用StringBuilder构建CSV内容
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        
        // 添加CSV头部
        printWriter.println("ID,药品名称,规格,类别,库存,有效期,供应商");
        
        // 添加数据行
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        drugs.forEach(drug -> {
            StringBuilder line = new StringBuilder();
            line.append(drug.getId()).append(",");
            line.append(escapeCSV(drug.getName())).append(",");
            line.append(escapeCSV(drug.getSpec())).append(",");
            line.append(escapeCSV(drug.getCategory())).append(",");
            line.append(drug.getStock()).append(",");
            line.append(drug.getExpirationDate() != null ? drug.getExpirationDate().format(formatter) : "").append(",");
            line.append(escapeCSV(drug.getSupplier()));
            printWriter.println(line.toString());
        });
        
        byte[] csvBytes = stringWriter.toString().getBytes(StandardCharsets.UTF_8);
        
        // 设置HTTP响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv;charset=UTF-8"));
        headers.setContentDispositionFormData("attachment", "drug_export.csv");
        headers.setContentLength(csvBytes.length);
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(csvBytes);
    }

    /**
     * 转义CSV字段中的特殊字符
     */
    private String escapeCSV(String field) {
        if (field == null) {
            return "";
        }
        
        // 如果字段包含逗号、双引号或换行符，则用双引号包围并将字段中的双引号替换为两个双引号
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        
        return field;
    }
} 