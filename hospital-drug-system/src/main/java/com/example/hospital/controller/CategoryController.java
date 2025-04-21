package com.example.hospital.controller;

import com.example.hospital.model.Category;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Map<Long, Category> CATEGORIES = new HashMap<>();
    private static long nextId = 1;

    static {
        // 添加一些测试数据
        Category c1 = new Category();
        c1.setId(nextId++);
        c1.setName("抗生素");
        c1.setDescription("抗菌类药物");
        CATEGORIES.put(c1.getId(), c1);

        Category c2 = new Category();
        c2.setId(nextId++);
        c2.setName("镇痛药");
        c2.setDescription("缓解疼痛类药物");
        CATEGORIES.put(c2.getId(), c2);

        Category c3 = new Category();
        c3.setId(nextId++);
        c3.setName("心血管药");
        c3.setDescription("治疗心血管疾病药物");
        CATEGORIES.put(c3.getId(), c3);
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        return ResponseEntity.ok(new ArrayList<>(CATEGORIES.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategoryById(@PathVariable Long id) {
        Category category = CATEGORIES.get(id);
        if (category == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(category);
    }
} 