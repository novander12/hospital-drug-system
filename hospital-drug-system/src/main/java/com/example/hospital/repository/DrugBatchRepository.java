package com.example.hospital.repository;

import com.example.hospital.model.DrugBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DrugBatchRepository extends JpaRepository<DrugBatch, Long> {

    // Find batches by drug ID
    List<DrugBatch> findByDrugId(Long drugId);

    // Find batches by drug ID, ordered by expiration date (useful for FEFO)
    List<DrugBatch> findByDrugIdOrderByExpirationDateAsc(Long drugId);

    // Find a specific batch by drug ID and batch number (if batch numbers are unique per drug)
    // Optional<DrugBatch> findByDrugIdAndBatchNumber(Long drugId, String batchNumber);

    // Add other custom queries as needed
} 