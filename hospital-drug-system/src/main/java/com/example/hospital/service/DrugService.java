package com.example.hospital.service;

import com.example.hospital.model.Drug;
import com.example.hospital.repository.DrugRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DrugService {

    @Autowired
    private DrugRepository drugRepository;

    public List<Drug> getAllDrugs() {
        return drugRepository.findAll();
    }

    public Optional<Drug> getDrugById(Long id) {
        return drugRepository.findById(id);
    }

    public Drug findDrugByName(String name) {
        return drugRepository.findByName(name);
    }

    @Transactional
    public Drug saveDrug(Drug drug) {
        return drugRepository.save(drug);
    }

    @Transactional
    public void deleteDrug(Long id) {
        drugRepository.deleteById(id);
    }

    public long countDrugs() {
        return drugRepository.count();
    }
    
    public List<Drug> searchDrugs(String keyword) {
        return drugRepository.findByNameContainingIgnoreCase(keyword);
    }
} 