package com.example.hospital.repository;

import com.example.hospital.model.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional; // Import Optional

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {

    // 可以根据需要添加查询方法，例如：
    // 按患者姓名查找处方
    List<Prescription> findByPatientNameContainingIgnoreCase(String patientName);

    // 按开方医生查找处方
    List<Prescription> findByPrescribingDoctorContainingIgnoreCase(String doctorName);

    // 查询特定状态的处方
    List<Prescription> findByStatus(Prescription.PrescriptionStatus status);

    // 使用 @Query 获取包含处方项的完整处方信息 (避免 N+1 问题)
    // 使用 Optional 包装结果，因为 findById 可能找不到记录
    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.items WHERE p.id = :id")
    Optional<Prescription> findByIdWithItems(Long id);

    // 获取所有包含处方项的完整处方信息
    @Query("SELECT p FROM Prescription p LEFT JOIN FETCH p.items")
    List<Prescription> findAllWithItems();
}