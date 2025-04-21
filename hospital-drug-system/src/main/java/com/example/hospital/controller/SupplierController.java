package com.example.hospital.controller;

import com.example.hospital.model.Supplier;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/suppliers")
public class SupplierController {

    private static final Map<Long, Supplier> SUPPLIERS = new HashMap<>();
    private static long nextId = 1;

    static {
        // 添加一些测试数据
        Supplier s1 = new Supplier();
        s1.setId(nextId++);
        s1.setName("北京医药有限公司");
        s1.setContact("张经理");
        s1.setPhone("010-12345678");
        s1.setAddress("北京市海淀区中关村大街1号");
        SUPPLIERS.put(s1.getId(), s1);

        Supplier s2 = new Supplier();
        s2.setId(nextId++);
        s2.setName("上海药业集团");
        s2.setContact("李经理");
        s2.setPhone("021-87654321");
        s2.setAddress("上海市浦东新区张江高科技园区");
        SUPPLIERS.put(s2.getId(), s2);

        Supplier s3 = new Supplier();
        s3.setId(nextId++);
        s3.setName("广州医药股份有限公司");
        s3.setContact("王总监");
        s3.setPhone("020-66778899");
        s3.setAddress("广州市越秀区东风东路");
        SUPPLIERS.put(s3.getId(), s3);
    }

    @GetMapping
    public ResponseEntity<List<Supplier>> getAllSuppliers() {
        return ResponseEntity.ok(new ArrayList<>(SUPPLIERS.values()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Supplier> getSupplierById(@PathVariable Long id) {
        Supplier supplier = SUPPLIERS.get(id);
        if (supplier == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(supplier);
    }
} 