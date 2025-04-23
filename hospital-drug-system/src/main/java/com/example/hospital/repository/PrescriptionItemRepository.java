package com.example.hospital.repository;

import com.example.hospital.model.PrescriptionItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PrescriptionItemRepository extends JpaRepository<PrescriptionItem, Long> {

    // 根据处方ID查找所有处方项
    List<PrescriptionItem> findByPrescriptionId(Long prescriptionId);

    // 根据药品ID查找相关的处方项 (例如，统计某个药品被开了多少次)
    List<PrescriptionItem> findByDrugId(Long drugId);
}