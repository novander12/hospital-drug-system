package com.example.hospital.controller;

import com.example.hospital.dto.DrugBatchCreateDTO;
import com.example.hospital.dto.DrugCreateDTO;
import com.example.hospital.dto.DrugDTO;
import com.example.hospital.model.Drug;
import com.example.hospital.model.DrugBatch;
import com.example.hospital.service.DrugService;
import com.example.hospital.service.OperationLogService;
import com.example.hospital.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/drugs")
public class DrugController {

    private final DrugService drugService;
    private final UserService userService;
    private final OperationLogService operationLogService;

    @Autowired
    public DrugController(DrugService drugService, UserService userService, OperationLogService operationLogService) {
        this.drugService = drugService;
        this.userService = userService;
        this.operationLogService = operationLogService;
    }

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
     * 获取所有药品 (DTO format)
     */
    @GetMapping
    public ResponseEntity<List<DrugDTO>> getAllDrugs() {
        List<DrugDTO> drugDTOs = drugService.getAllDrugs();
        return ResponseEntity.ok(drugDTOs);
    }

    /**
     * 根据ID获取药品 (DTO format)
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getDrugById(@PathVariable Long id) {
        Optional<DrugDTO> drugDTO = drugService.getDrugDTOById(id);
        if (drugDTO.isPresent()) {
            return ResponseEntity.ok(drugDTO.get());
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("药品不存在"));
        }
    }

    /**
     * 创建新药品 (核心信息)
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createDrug(@Valid @RequestBody DrugCreateDTO drugCreateDTO) {
        try {
            Drug savedDrug = drugService.createDrug(drugCreateDTO);
            // Return the DTO representation
            Optional<DrugDTO> drugDTO = drugService.getDrugDTOById(savedDrug.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(drugDTO.orElse(null)); // Or return a success message
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("创建药品失败: " + e.getMessage()));
        }
    }

    /**
     * 更新药品信息 (核心信息)
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateDrug(@PathVariable Long id, @Valid @RequestBody DrugCreateDTO drugUpdateDTO) {
         try {
            Drug updatedDrug = drugService.updateDrug(id, drugUpdateDTO);
            Optional<DrugDTO> drugDTO = drugService.getDrugDTOById(updatedDrug.getId());
            return ResponseEntity.ok(drugDTO.orElse(null));
        } catch (javax.persistence.EntityNotFoundException e) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("更新药品失败: " + e.getMessage()));
        }
    }

    /**
     * 删除药品 (包括其所有批次)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteDrug(@PathVariable Long id) {
        try {
            drugService.deleteDrug(id);
            return ResponseEntity.ok(createSuccessResponse("药品及其批次删除成功", null));
        } catch (javax.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("删除药品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 为指定药品添加新批次
     */
    @PostMapping("/{drugId}/batches")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> addBatchToDrug(@PathVariable Long drugId, @Valid @RequestBody DrugBatchCreateDTO batchCreateDTO) {
        try {
            DrugBatch savedBatch = drugService.addBatchToDrug(drugId, batchCreateDTO);
            // Optionally return the created batch DTO or just success
            return ResponseEntity.status(HttpStatus.CREATED).body(createSuccessResponse("批次添加成功", savedBatch)); 
        } catch (javax.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("添加批次失败: " + e.getMessage()));
        }
    }
    
     /**
     * 获取指定药品的所有批次
     */
    @GetMapping("/{drugId}/batches")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // Allow all authenticated users to view batches
    public ResponseEntity<?> getDrugBatches(@PathVariable Long drugId) {
        // Check if drug exists first (optional, service might handle it)
        if (!drugService.getDrugEntityById(drugId).isPresent()) {
             return ResponseEntity.status(HttpStatus.NOT_FOUND).body(createErrorResponse("药品不存在"));
        }
        try {
            List<DrugBatch> batches = drugService.findBatchesByDrugId(drugId);
            // Convert to DTO if necessary, or return entity list if simple enough
            return ResponseEntity.ok(batches);
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("获取批次失败: " + e.getMessage()));
        }
    }

    /**
     * 搜索药品 (返回 DTO 列表)
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchDrugsByName(@RequestParam String name) {
        try {
            List<DrugDTO> drugDTOs = drugService.searchDrugs(name);
            return ResponseEntity.ok(drugDTOs);
        } catch (Exception e) {
             return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("搜索药品失败: " + e.getMessage()));
        }
    }
    
    /**
     * 获取所有不同的供应商名称
     */
    @GetMapping("/suppliers")
    public ResponseEntity<?> getDistinctSuppliers() {
        try {
            List<String> suppliers = drugService.getDistinctSuppliers();
            return ResponseEntity.ok(suppliers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(createErrorResponse("获取供应商列表失败: " + e.getMessage()));
        }
    }

    /**
     * 获取指定天数内过期的药品列表
     */
    @GetMapping("/expiring")
    @PreAuthorize("isAuthenticated()") // 允许登录用户访问
    public ResponseEntity<?> getExpiringDrugs(@RequestParam(defaultValue = "30") int days) {
        try {
            List<DrugDTO> expiringDrugs = drugService.findExpiringDrugsDTO(days);
            // 直接返回DTO列表，前端需要的格式
             return ResponseEntity.ok(expiringDrugs); 
            // 如果需要符合之前的 success/error 结构:
            // return ResponseEntity.ok(createSuccessResponse("获取过期预警药品成功", expiringDrugs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(createErrorResponse("获取过期预警药品失败: " + e.getMessage()));
        }
    }

    /**
     * 导出所有药品信息为 CSV 文件
     */
    @GetMapping("/export")
    @PreAuthorize("hasRole('ADMIN')") // 仅管理员可导出
    public ResponseEntity<String> exportDrugsToCsv() {
        try {
            List<DrugDTO> drugs = drugService.getAllDrugs(); // 获取所有药品 DTO
            String csvData = convertDrugsToCsv(drugs);

            HttpHeaders headers = new HttpHeaders();
            headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"drugs_export.csv\"");
            headers.add(HttpHeaders.CONTENT_TYPE, "text/csv; charset=utf-8"); // 指定 UTF-8 编码

            return new ResponseEntity<>(csvData, headers, HttpStatus.OK);
        } catch (Exception e) {
            // Log the exception
            // logger.error("导出药品CSV失败", e);
            // 返回更通用的错误信息给客户端
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("导出药品列表失败");
        }
    }

    // --- Helper methods for CSV conversion and response --- 
    private String convertDrugsToCsv(List<DrugDTO> drugs) {
        StringBuilder csvBuilder = new StringBuilder();
        // 添加 UTF-8 BOM 头, 防止 Excel 打开乱码 (可选但推荐)
        csvBuilder.append("\uFEFF"); 
        // CSV Header
        csvBuilder.append("ID,药品名称,规格,类别,总库存,供应商\n");

        // CSV Rows
        for (DrugDTO drug : drugs) {
            csvBuilder.append(escapeCsvField(drug.getId())).append(",");
            csvBuilder.append(escapeCsvField(drug.getName())).append(",");
            csvBuilder.append(escapeCsvField(drug.getSpec())).append(",");
            csvBuilder.append(escapeCsvField(drug.getCategory())).append(",");
            csvBuilder.append(escapeCsvField(drug.getTotalStock())).append(",");
            csvBuilder.append(escapeCsvField(drug.getSupplier())).append("\n");
        }
        return csvBuilder.toString();
    }

    private String escapeCsvField(Object field) {
        if (field == null) {
            return "";
        }
        String fieldStr = String.valueOf(field);
        // 如果字段包含逗号、双引号或换行符，则用双引号括起来，并将内部双引号转义为两个双引号
        if (fieldStr.contains(",") || fieldStr.contains("\"") || fieldStr.contains("\n")) {
            return "\"" + fieldStr.replace("\"", "\"\"") + "\"";
        }
        return fieldStr;
    }

    // --- Helper methods for response --- 
    private Map<String, Object> createSuccessResponse(String message, Object data) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", message);
        if (data != null) {
            response.put("data", data);
        }
        return response;
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "error");
        response.put("message", message);
        return response;
    }
} 