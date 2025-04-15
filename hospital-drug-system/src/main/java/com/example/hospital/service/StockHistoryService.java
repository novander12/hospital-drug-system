package com.example.hospital.service;

import com.example.hospital.model.Drug;
import com.example.hospital.model.StockHistory;
import com.example.hospital.repository.DrugRepository;
import com.example.hospital.repository.StockHistoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class StockHistoryService {

    @Autowired
    private DrugRepository drugRepository;
    
    @Autowired
    private StockHistoryRepository stockHistoryRepository;
    
    /**
     * 每天午夜记录当天的库存总量
     */
    @Scheduled(cron = "0 0 0 * * ?") // 每天午夜运行
    @Transactional
    public void recordDailyStockSnapshot() {
        // 获取所有药品
        List<Drug> allDrugs = drugRepository.findAll();
        
        // 计算当前总库存
        int totalStock = allDrugs.stream()
                .mapToInt(drug -> drug.getStock() != null ? drug.getStock() : 0)
                .sum();
        
        // 获取今天的日期
        LocalDate today = LocalDate.now();
        
        // 检查是否已经有今天的记录
        StockHistory existingRecord = stockHistoryRepository.findByDate(today);
        
        if (existingRecord != null) {
            // 更新现有记录
            existingRecord.setTotalStock(totalStock);
            stockHistoryRepository.save(existingRecord);
        } else {
            // 创建新记录
            StockHistory newRecord = new StockHistory(today, totalStock);
            stockHistoryRepository.save(newRecord);
        }
    }
    
    /**
     * 在应用启动时记录当天的库存总量
     */
    @Transactional
    public void recordStockSnapshotOnStartup() {
        // 获取所有药品
        List<Drug> allDrugs = drugRepository.findAll();
        
        // 计算当前总库存
        int totalStock = allDrugs.stream()
                .mapToInt(drug -> drug.getStock() != null ? drug.getStock() : 0)
                .sum();
        
        // 获取今天的日期
        LocalDate today = LocalDate.now();
        
        // 检查是否已经有今天的记录
        StockHistory existingRecord = stockHistoryRepository.findByDate(today);
        
        if (existingRecord != null) {
            // 更新现有记录
            existingRecord.setTotalStock(totalStock);
            stockHistoryRepository.save(existingRecord);
        } else {
            // 创建新记录
            StockHistory newRecord = new StockHistory(today, totalStock);
            stockHistoryRepository.save(newRecord);
        }
        
        // 如果不存在过去30天的记录，创建模拟数据
        createMockHistoryIfNeeded();
    }
    
    /**
     * 创建模拟历史数据（如果需要）
     */
    private void createMockHistoryIfNeeded() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        List<StockHistory> history = stockHistoryRepository.findStockHistoryInRange(startDate);
        
        // 如果不存在历史数据，创建模拟数据
        if (history.isEmpty()) {
            int baseStock = 1000; // 基础库存量
            
            for (int i = 30; i > 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                // 随机波动，创建更真实的数据
                int randomVariation = (int) (Math.random() * 100) - 50; // -50 到 50 之间的随机数
                int dailyStock = baseStock + randomVariation;
                
                StockHistory mockRecord = new StockHistory(date, dailyStock);
                stockHistoryRepository.save(mockRecord);
                
                // 更新基础库存，产生趋势
                baseStock += (int) (Math.random() * 20) - 5; // -5 到 15 之间的随机数
            }
        }
    }
} 