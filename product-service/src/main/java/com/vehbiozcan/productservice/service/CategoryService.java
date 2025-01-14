package com.vehbiozcan.productservice.service;

import com.vehbiozcan.productservice.model.Category;
import com.vehbiozcan.productservice.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public Category add(Category category) {
        return categoryRepository.save(category);
    }
}
