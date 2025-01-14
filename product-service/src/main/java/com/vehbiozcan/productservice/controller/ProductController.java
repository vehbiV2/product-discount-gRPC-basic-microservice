package com.vehbiozcan.productservice.controller;

import com.vehbiozcan.productservice.model.Product;
import com.vehbiozcan.productservice.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/products")
public class ProductController {

    private final ProductService productService;

    @PostMapping("/add")
    public ResponseEntity<Product> addProduct(@RequestBody  Product product) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.addProduct(product));
    }

    @GetMapping("all")
    public ResponseEntity<List<Product>> getProducts() {
        return ResponseEntity.status(HttpStatus.OK).body(productService.getAllProducts());
    }

}
