package com.example.hospital.config;

import com.example.hospital.model.Drug;
import com.example.hospital.model.User;
import com.example.hospital.model.OperationLog;
import com.example.hospital.service.DrugService;
import com.example.hospital.service.UserService;
import com.example.hospital.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserService userService;

    @Autowired
    private DrugService drugService;

    @Autowired
    private OperationLogService logService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeDrugs();
        initializeOperationLogs();
    }

    private void initializeUsers() {
        // 检查并创建管理员用户
        User existingAdmin = userService.findByUsername("admin");
        if (existingAdmin == null) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            String rawPassword = "admin123";
            adminUser.setPassword(rawPassword);
            adminUser.setRole("ADMIN");
            User savedAdmin = userService.createUser(adminUser);
            System.out.println("已创建默认管理员用户: admin/admin123");
            System.out.println("加密后密码: " + savedAdmin.getPassword());
        } else {
            System.out.println("已存在管理员用户: " + existingAdmin.getUsername());
        }

        // 检查并创建普通用户
        if (userService.findByUsername("user") == null) {
            User normalUser = new User();
            normalUser.setUsername("user");
            String rawPassword = "user123";
            normalUser.setPassword(rawPassword);
            normalUser.setRole("USER");
            User savedUser = userService.createUser(normalUser);
            System.out.println("已创建默认普通用户: user/user123");
            System.out.println("加密后密码: " + savedUser.getPassword());
        }
    }

    private void initializeDrugs() {
        // 检查是否有药品数据，如果没有则添加
        if (drugService.countDrugs() == 0) {
            List<Drug> drugs = Arrays.asList(
                new Drug("阿莫西林胶囊", "0.25g*24粒", "国药集团", "抗生素", 50, "盒", 15.5, LocalDate.of(2026, 10, 31)),
                new Drug("布洛芬缓释胶囊", "0.3g*20粒", "强生制药", "解热镇痛", 100, "盒", 22.0, LocalDate.of(2025, 12, 15)),
                new Drug("维生素C片", "100mg*100片", "拜耳医药", "维生素", 200, "瓶", 9.9, LocalDate.of(2027, 5, 20)),
                new Drug("奥美拉唑肠溶胶囊", "20mg*14粒", "阿斯利康", "消化系统", 80, "盒", 35.8, LocalDate.of(2026, 8, 8)),
                new Drug("蒙脱石散", "3g*10袋", "博福-益普生", "止泻药", 120, "盒", 18.5, LocalDate.of(2025, 11, 30)),
                new Drug("复方甘草片", "100片", "哈药集团", "止咳化痰", 150, "瓶", 8.0, LocalDate.of(2026, 6, 1)),
                new Drug("创可贴", "100片", "邦迪", "外用", 300, "盒", 12.0, LocalDate.of(2027, 1, 10)),
                new Drug("碘伏消毒液", "100ml", "利尔康", "消毒剂", 50, "瓶", 5.5, LocalDate.of(2026, 9, 5))
            );
            drugs.forEach(drugService::saveDrug);
            System.out.println("已初始化药品数据: " + drugs.size() + "条记录");
        } else {
            System.out.println("药品数据已存在，跳过初始化。");
        }
    }

    private void initializeOperationLogs() {
        // 检查是否有操作日志，如果没有则添加一些模拟日志
        if (logService.countLogs() == 0) {
             User admin = userService.findByUsername("admin");
             User user = userService.findByUsername("user");
             Drug drug1 = drugService.findDrugByName("阿莫西林胶囊");
             Drug drug2 = drugService.findDrugByName("布洛芬缓释胶囊");

             if (admin != null && user != null && drug1 != null && drug2 != null) {
                 List<OperationLog> logs = Arrays.asList(
                     new OperationLog(admin.getUsername(), "药品入库", drug1.getId(), drug1.getName(), "入库数量: 20", LocalDateTime.now().minusDays(2)),
                     new OperationLog(user.getUsername(), "药品出库", drug2.getId(), drug2.getName(), "出库数量: 10", LocalDateTime.now().minusDays(1).minusHours(5)),
                     new OperationLog(admin.getUsername(), "修改药品", drug1.getId(), drug1.getName(), "库存修改为: 70", LocalDateTime.now().minusHours(10)),
                     new OperationLog(user.getUsername(), "查看药品", drug2.getId(), drug2.getName(), "用户查看药品详情", LocalDateTime.now().minusHours(2)),
                     new OperationLog(admin.getUsername(), "添加药品", null, "新药品X", "添加新药品", LocalDateTime.now().minusMinutes(30))
                 );
                 logs.forEach(logService::saveLog);
                 System.out.println("已初始化操作日志数据: " + logs.size() + "条记录");
             }
        } else {
             System.out.println("操作日志数据已存在，跳过初始化。");
        }
    }
} 