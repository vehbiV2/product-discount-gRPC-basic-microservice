package com.vehbiozcan.productservice.controller;

import com.vehbiozcan.productservice.model.Category;
import com.vehbiozcan.productservice.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    @PostMapping("add")
    public ResponseEntity<Category> add(@RequestBody Category category) {
        return ResponseEntity.status(HttpStatus.CREATED).body(categoryService.add(category));
    }

}
