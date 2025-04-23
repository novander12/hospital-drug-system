package com.example.hospital.config;

import com.example.hospital.model.*;
import com.example.hospital.dto.*;
import com.example.hospital.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired private UserService userService;
    @Autowired private DrugService drugService;
    @Autowired private OperationLogService logService;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private PrescriptionService prescriptionService;

    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeDrugs();
        initializePrescriptionsAndConsumption();
        initializeOperationLogs();
    }

    private void initializeUsers() {
        if (userService.findByUsername("admin") == null) {
            userService.createUser("admin", "admin123", Role.ADMIN);
            System.out.println("已创建默认管理员用户: admin/admin123");
        } else { System.out.println("已存在管理员用户: admin"); }
        if (userService.findByUsername("user") == null) {
            userService.createUser("user", "user123", Role.USER);
            System.out.println("已创建默认普通用户: user/user123");
        } else { System.out.println("已存在普通用户: user"); }
        if (userService.findByUsername("pharmacist") == null) {
            userService.createUser("pharmacist", "pharmacist123", Role.PHARMACIST);
            System.out.println("已创建默认药师用户: pharmacist/pharmacist123");
        } else { System.out.println("已存在药师用户: pharmacist"); }
        if (userService.findByUsername("nurse") == null) {
            userService.createUser("nurse", "nurse123", Role.NURSE);
            System.out.println("已创建默认护士用户: nurse/nurse123");
        } else { System.out.println("已存在护士用户: nurse"); }
    }

    private void initializeDrugs() {
        if (drugService.countDrugs() == 0) {
            System.out.println("Initializing drugs...");
            
            // Create Drugs using DTO with setters
            DrugCreateDTO drug1DTO = new DrugCreateDTO();
            drug1DTO.setName("阿莫西林胶囊"); drug1DTO.setSpec("0.25g*24粒"); drug1DTO.setCategory("抗生素"); drug1DTO.setSupplier("国药集团");
            Drug drug1 = drugService.createDrug(drug1DTO);
            
            DrugCreateDTO drug2DTO = new DrugCreateDTO();
            drug2DTO.setName("布洛芬缓释胶囊"); drug2DTO.setSpec("0.3g*20粒"); drug2DTO.setCategory("解热镇痛"); drug2DTO.setSupplier("强生制药");
            Drug drug2 = drugService.createDrug(drug2DTO);
            
            DrugCreateDTO drug3DTO = new DrugCreateDTO();
            drug3DTO.setName("维生素C片"); drug3DTO.setSpec("100mg*100片"); drug3DTO.setCategory("维生素"); drug3DTO.setSupplier("拜耳医药");
            Drug drug3 = drugService.createDrug(drug3DTO);

            DrugCreateDTO drug4DTO = new DrugCreateDTO();
            drug4DTO.setName("蒙脱石散"); drug4DTO.setSpec("3g*10袋"); drug4DTO.setCategory("止泻药"); drug4DTO.setSupplier("博福-益普生");
            Drug drug4 = drugService.createDrug(drug4DTO);
            
            DrugCreateDTO drug5DTO = new DrugCreateDTO();
            drug5DTO.setName("奥美拉唑肠溶胶囊"); drug5DTO.setSpec("20mg*14粒"); drug5DTO.setCategory("消化系统"); drug5DTO.setSupplier("阿斯利康");
            Drug drug5 = drugService.createDrug(drug5DTO);
            
            DrugCreateDTO drug6DTO = new DrugCreateDTO();
            drug6DTO.setName("氯雷他定片"); drug6DTO.setSpec("10mg*10片"); drug6DTO.setCategory("抗过敏"); drug6DTO.setSupplier("先灵葆雅");
            Drug drug6 = drugService.createDrug(drug6DTO);

            // Add batches using DTO with setters
            DrugBatchCreateDTO batch1_1 = new DrugBatchCreateDTO();
            batch1_1.setBatchNumber("AMX202401"); batch1_1.setProductionDate(LocalDate.of(2024, 1, 1)); batch1_1.setExpirationDate(LocalDate.of(2026, 12, 31)); batch1_1.setQuantity(50); batch1_1.setSupplier("国药集团");
            drugService.addBatchToDrug(drug1.getId(), batch1_1);
            
            DrugBatchCreateDTO batch1_2 = new DrugBatchCreateDTO();
            batch1_2.setBatchNumber("AMX202406"); batch1_2.setProductionDate(LocalDate.of(2024, 6, 1)); batch1_2.setExpirationDate(LocalDate.of(2027, 5, 31)); batch1_2.setQuantity(100); batch1_2.setSupplier("国药集团");
            drugService.addBatchToDrug(drug1.getId(), batch1_2);

            DrugBatchCreateDTO batch2_1 = new DrugBatchCreateDTO();
            batch2_1.setBatchNumber("IBU202403"); batch2_1.setProductionDate(LocalDate.of(2024, 3, 15)); batch2_1.setExpirationDate(LocalDate.of(2025, 12, 15)); batch2_1.setQuantity(150); batch2_1.setSupplier("强生制药");
            drugService.addBatchToDrug(drug2.getId(), batch2_1);
            
            DrugBatchCreateDTO batch3_1 = new DrugBatchCreateDTO();
            batch3_1.setBatchNumber("VITC202405"); batch3_1.setProductionDate(LocalDate.of(2024, 5, 10)); batch3_1.setExpirationDate(LocalDate.of(2027, 4, 30)); batch3_1.setQuantity(200); batch3_1.setSupplier("拜耳医药");
            drugService.addBatchToDrug(drug3.getId(), batch3_1);

            DrugBatchCreateDTO batch3_2 = new DrugBatchCreateDTO();
            batch3_2.setBatchNumber("VITC202407"); batch3_2.setProductionDate(LocalDate.of(2024, 7, 1)); batch3_2.setExpirationDate(LocalDate.of(2027, 6, 30)); batch3_2.setQuantity(150); batch3_2.setSupplier("拜耳医药");
            drugService.addBatchToDrug(drug3.getId(), batch3_2);

            DrugBatchCreateDTO batch4_1 = new DrugBatchCreateDTO();
            batch4_1.setBatchNumber("MON202402"); batch4_1.setProductionDate(LocalDate.of(2024, 2, 20)); batch4_1.setExpirationDate(LocalDate.of(2026, 1, 31)); batch4_1.setQuantity(80); batch4_1.setSupplier("博福-益普生");
            drugService.addBatchToDrug(drug4.getId(), batch4_1);

            DrugBatchCreateDTO batch5_1 = new DrugBatchCreateDTO();
            batch5_1.setBatchNumber("OME202404"); batch5_1.setProductionDate(LocalDate.of(2024, 4, 5)); batch5_1.setExpirationDate(LocalDate.of(2026, 3, 31)); batch5_1.setQuantity(120); batch5_1.setSupplier("阿斯利康");
            drugService.addBatchToDrug(drug5.getId(), batch5_1);

            DrugBatchCreateDTO batch6_1 = new DrugBatchCreateDTO();
            batch6_1.setBatchNumber("LOR202406"); batch6_1.setProductionDate(LocalDate.of(2024, 6, 15)); batch6_1.setExpirationDate(LocalDate.of(2027, 5, 31)); batch6_1.setQuantity(60); batch6_1.setSupplier("先灵葆雅");
            drugService.addBatchToDrug(drug6.getId(), batch6_1);

            System.out.println("Initialized 6 drugs with multiple batches.");
        } else {
            System.out.println("药品数据已存在，跳过初始化。");
        }
    }
    
    private void initializePrescriptionsAndConsumption() {
         // We cannot reliably check if prescriptions exist without a count method,
         // so we might create duplicates if run multiple times. 
         // A proper check or ensuring the initializer runs only once is needed in production.
         System.out.println("Attempting to initialize prescriptions and simulate consumption...");
         
         User pharmacist = userService.findByUsername("pharmacist");
         User nurse = userService.findByUsername("nurse");
         
         // Find drugs using the correct method
         Drug drug1 = drugService.getDrugEntityById(1L).orElse(null); // Amoxicillin
         Drug drug2 = drugService.getDrugEntityById(2L).orElse(null); // Ibuprofen
         Drug drug4 = drugService.getDrugEntityById(4L).orElse(null); // Montmorillonite
         Drug drug6 = drugService.getDrugEntityById(6L).orElse(null); // Loratadine

         if (drug1 == null || drug2 == null || drug4 == null || drug6 == null || pharmacist == null || nurse == null) {
             System.err.println("无法初始化处方数据：基础用户或药品信息缺失！");
             return;
         }

         try {
             // Simulate running as pharmacist for creation
             // NOTE: This is a workaround for initializers not having a security context.
             // In real scenarios, the service layer handles the current user.
             // We might need a dedicated data seeding service or method in UserService if needed.

             // Prescription 1 (Completed)
             PrescriptionCreateDTO p1DTO = new PrescriptionCreateDTO();
             p1DTO.setPatientName("张三"); p1DTO.setPatientAge(30); p1DTO.setPatientGender("男"); p1DTO.setPrescribingDoctor("王医生");
             p1DTO.setDiagnosis("上呼吸道感染"); p1DTO.setPrescriptionDate(LocalDate.now().minusDays(5));
             List<PrescriptionItemDTO> p1Items = new ArrayList<>();
             PrescriptionItemDTO p1Item1 = new PrescriptionItemDTO();
             p1Item1.setDrugId(drug1.getId()); p1Item1.setQuantity(2); p1Item1.setDosage("每日三次，每次一粒"); p1Item1.setFrequency("TID"); p1Item1.setNotes("饭后服用");
             p1Items.add(p1Item1);
             p1DTO.setItems(p1Items);
             // Need a way to set the 'creator' context or use a service method that allows specifying creator
             // For now, we cannot directly call createPrescription which relies on SecurityContext
             // We will skip creating prescriptions directly here until context can be handled.
             // TODO: Revisit prescription creation in initializer
             System.out.println("Skipping prescription initialization due to Security Context limitations in Initializer.");
             System.out.println("Please create prescriptions manually or adjust service layer for seeding.");

            /* // --- CODE TO CREATE/UPDATE PRESCRIPTIONS (Needs Security Context Handling) --- 
            Prescription p1 = prescriptionService.createPrescription(p1DTO); // Fails without context
            // Simulate updates (also needs context or modified service methods)
            prescriptionService.updatePrescriptionStatus(p1.getId(), Prescription.PrescriptionStatus.APPROVED, "药师审核通过");
            prescriptionService.updatePrescriptionStatus(p1.getId(), Prescription.PrescriptionStatus.DISPENSED, "护士发药完成");
            System.out.println("Simulated creation and completion for prescription 1 (Amoxicillin x2)");
            */

         } catch (Exception e) {
             System.err.println("初始化处方及模拟消耗时出错: " + e.getMessage());
             e.printStackTrace();
         }
    }

    private void initializeOperationLogs() {
        if (logService.countLogs() == 0) {
            System.out.println("Initializing operation logs...");
             User admin = userService.findByUsername("admin");
             User pharmacist = userService.findByUsername("pharmacist");
             User nurse = userService.findByUsername("nurse");
            
            Long drug1Id = 1L; String drug1Name = "阿莫西林胶囊";
            Long drug2Id = 2L; String drug2Name = "布洛芬缓释胶囊";
             
             if (admin != null && pharmacist != null && nurse != null) { 
                 List<OperationLog> logs = Arrays.asList(
                     new OperationLog(admin.getUsername(), "添加药品", "SUCCESS", "创建新药品条目", drug1Id, drug1Name, LocalDateTime.now().minusDays(3)),
                     new OperationLog(pharmacist.getUsername(), "添加批次", "SUCCESS", "批号: AMX202406, 数量: 100", drug1Id, drug1Name, LocalDateTime.now().minusDays(2)),
                     new OperationLog(pharmacist.getUsername(), "处方审核", "SUCCESS", "审核通过 (处方ID: 1)", 1L, "处方相关", LocalDateTime.now().minusDays(1)),
                     new OperationLog(nurse.getUsername(), "处方发药", "SUCCESS", "发药完成 (处方ID: 1)", 1L, "处方相关", LocalDateTime.now().minusHours(5)),
                     new OperationLog(admin.getUsername(), "查看报告", "SUCCESS", "生成库存报告", null, "库存报告", LocalDateTime.now().minusHours(2))
                 );
                 logs.forEach(logService::saveLog);
                 System.out.println("已初始化操作日志数据: " + logs.size() + "条记录");
             }
        } else {
             System.out.println("操作日志数据已存在，跳过初始化。");
        }
    }
} 