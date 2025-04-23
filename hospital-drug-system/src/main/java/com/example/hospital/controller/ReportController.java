package com.example.hospital.controller;

import com.example.hospital.dto.InventoryReportItemDTO;
import com.example.hospital.dto.DrugConsumptionDTO;
import com.example.hospital.service.ReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    @Autowired
    private ReportService reportService;

    /**
     * 获取库存报告数据 (JSON)
     * @return 库存报告数据列表
     */
    @GetMapping("/inventory")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<InventoryReportItemDTO>> getInventoryReport() {
        List<InventoryReportItemDTO> reportData = reportService.generateInventoryReport();
        return ResponseEntity.ok(reportData);
    }

    /**
     * 导出库存报告为 CSV 文件
     * @return CSV 文件内容
     */
    @GetMapping("/inventory/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportInventoryReportAsCsv() {
        List<InventoryReportItemDTO> reportData = reportService.generateInventoryReport();
        String csvData = convertToCsv(reportData);

        HttpHeaders headers = new HttpHeaders();
        String filename = "inventory_report_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8")); // Ensure UTF-8 for Chinese characters

        return ResponseEntity.ok()
                .headers(headers)
                .body("\uFEFF" + csvData); // Add BOM for Excel compatibility with UTF-8
    }

    /**
     * 获取药品消耗统计报告 (JSON)
     * @param startDate 开始日期 (ISO format: yyyy-MM-dd)
     * @param endDate 结束日期 (ISO format: yyyy-MM-dd)
     * @return 药品消耗报告数据列表
     */
    @GetMapping("/consumption")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<DrugConsumptionDTO>> getDrugConsumptionReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        
        // Basic validation: endDate should not be before startDate
        if (endDate.isBefore(startDate)) {
            // Consider returning a specific error response DTO later
            return ResponseEntity.badRequest().body(null); 
        }
        
        List<DrugConsumptionDTO> reportData = reportService.generateDrugConsumptionReport(startDate, endDate);
        return ResponseEntity.ok(reportData);
    }

    /**
     * 导出药品消耗统计报告为 CSV 文件
     * @param startDate 开始日期 (ISO format: yyyy-MM-dd)
     * @param endDate 结束日期 (ISO format: yyyy-MM-dd)
     * @return CSV 文件内容
     */
    @GetMapping("/consumption/export")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> exportDrugConsumptionReportAsCsv(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Basic validation: endDate should not be before startDate
        if (endDate.isBefore(startDate)) {
            return ResponseEntity.badRequest().body("结束日期不能早于开始日期");
        }

        List<DrugConsumptionDTO> reportData = reportService.generateDrugConsumptionReport(startDate, endDate);
        String csvData = convertConsumptionToCsv(reportData); // Use the new converter method

        HttpHeaders headers = new HttpHeaders();
        String filename = String.format("drug_consumption_report_%s_to_%s.csv", 
                                       startDate.format(DateTimeFormatter.ISO_DATE), 
                                       endDate.format(DateTimeFormatter.ISO_DATE));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setContentType(MediaType.parseMediaType("text/csv; charset=utf-8")); // Ensure UTF-8

        return ResponseEntity.ok()
                .headers(headers)
                .body("\\uFEFF" + csvData); // Add BOM for Excel
    }

    /**
     * 将报告数据列表转换为 CSV 格式字符串
     * @param data 报告数据列表
     * @return CSV 格式字符串
     */
    private String convertToCsv(List<InventoryReportItemDTO> data) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            // 写入 CSV 头部
            pw.println("药品ID,药品名称,规格,类别,供应商,当前总库存");

            // 写入数据行
            data.forEach(item -> pw.println(
                    item.getDrugId() + "," +
                    escapeCsv(item.getDrugName()) + "," +
                    escapeCsv(item.getSpec()) + "," +
                    escapeCsv(item.getCategory()) + "," +
                    escapeCsv(item.getSupplier()) + "," +
                    item.getTotalStock()
            ));
        }
        return sw.toString();
    }

    /**
     * 将药品消耗报告数据列表转换为 CSV 格式字符串 (DrugConsumptionDTO)
     * @param data 药品消耗报告数据列表
     * @return CSV 格式字符串
     */
    private String convertConsumptionToCsv(List<DrugConsumptionDTO> data) {
        StringWriter sw = new StringWriter();
        try (PrintWriter pw = new PrintWriter(sw)) {
            // 写入 CSV 头部
            pw.println("药品ID,药品名称,规格,类别,消耗总量");

            // 写入数据行
            data.forEach(item -> pw.println(
                    item.getDrugId() + "," +
                    escapeCsv(item.getDrugName()) + "," +
                    escapeCsv(item.getSpec()) + "," +
                    escapeCsv(item.getCategory()) + "," +
                    item.getTotalConsumedQuantity() // Assuming Long, no need to escape
            ));
        }
        return sw.toString();
    }

    /**
     * 处理 CSV 特殊字符（例如逗号、引号）
     * 如果字段包含逗号或引号，则用双引号括起来，并将字段内的双引号转义为两个双引号。
     * @param field 字段值
     * @return 转义后的字段值
     */
    private String escapeCsv(String field) {
        if (field == null) {
            return "";
        }
        String escapedField = field.replace("\"", "\"\""); // Escape double quotes
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            escapedField = "\"" + escapedField + "\""; // Enclose in double quotes
        }
        return escapedField;
    }
}
