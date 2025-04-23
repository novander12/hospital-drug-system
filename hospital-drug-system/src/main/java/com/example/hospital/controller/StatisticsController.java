package com.example.hospital.controller;

import com.example.hospital.model.Drug;
import com.example.hospital.model.StockHistory;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.repository.StockHistoryRepository;
import com.example.hospital.service.DrugService;
import com.example.hospital.dto.DrugDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 统计数据控制器，提供各类数据统计接口
 */
@RestController
@RequestMapping("/api/statistics")
public class StatisticsController {

    @Autowired
    private DrugService drugService;

    @Autowired
    private StockHistoryRepository stockHistoryRepository;

    /**
     * 获取按药品类别分组的统计
     */
    @GetMapping("/category")
    public ResponseEntity<?> getStatisticsByCategory() {
        List<DrugDTO> drugs = drugService.getAllDrugs();
        
        Map<String, Long> categoryCountMap = drugs.stream()
                .filter(dto -> dto.getCategory() != null && !dto.getCategory().isEmpty())
                .collect(Collectors.groupingBy(
                        DrugDTO::getCategory,
                        Collectors.counting()
                ));
        
        List<Map<String, Object>> result = new ArrayList<>();
        categoryCountMap.forEach((category, count) -> {
            Map<String, Object> item = new HashMap<>();
            item.put("category", category);
            item.put("count", count);
            result.add(item);
        });
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * 统计药品库存情况
     * @return 库存情况统计
     */
    @GetMapping("/inventory")
    public ResponseEntity<?> getInventoryStats() {
        List<DrugDTO> allDrugs = drugService.getAllDrugs();
        
        int totalDrugs = allDrugs.size();
        int lowStockCount = 0;
        int normalStockCount = 0;
        int highStockCount = 0;
        
        final int LOW_STOCK_THRESHOLD = 10;
        final int HIGH_STOCK_THRESHOLD = 100;
        
        for (DrugDTO drug : allDrugs) {
            int stock = drug.getTotalStock() != null ? drug.getTotalStock() : 0;
            if (stock < LOW_STOCK_THRESHOLD) {
                lowStockCount++;
            } else if (stock > HIGH_STOCK_THRESHOLD) {
                highStockCount++;
            } else {
                normalStockCount++;
            }
        }
        
        Map<String, Object> stockData = new HashMap<>();
        stockData.put("lowStock", lowStockCount);
        stockData.put("normalStock", normalStockCount);
        stockData.put("highStock", highStockCount);
        stockData.put("totalDrugs", totalDrugs);
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "库存情况统计完成");
        response.put("data", stockData);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 为兼容性提供的别名接口
     */
    @GetMapping("/drugs")
    public ResponseEntity<?> getDrugStats() {
        return getStatisticsByCategory();
    }

    /**
     * 获取库存历史统计
     */
    @GetMapping("/stock-history")
    public ResponseEntity<?> getStockHistory(@RequestParam(value = "days", defaultValue = "30") int days) {
        LocalDate startDate = LocalDate.now().minusDays(days);
        List<StockHistory> stockHistoryList = stockHistoryRepository.findStockHistoryInRange(startDate);
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        List<Map<String, Object>> result = stockHistoryList.stream().map(history -> {
            Map<String, Object> item = new HashMap<>();
            item.put("date", history.getDate().format(formatter));
            item.put("totalStock", history.getTotalStock());
            return item;
        }).collect(Collectors.toList());
        
        return ResponseEntity.ok(result);
    }
} 