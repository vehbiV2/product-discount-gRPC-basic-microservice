package com.vehbiozcan.productservice.controller;

import com.vehbiozcan.grpc.DiscountResponse;
import com.vehbiozcan.productservice.model.dto.DiscountResponseDto;
import com.vehbiozcan.productservice.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/discount")
public class DiscountController {

    private final DiscountService discountService;


    @GetMapping
    public ResponseEntity<DiscountResponseDto> getDiscount(@RequestParam("productId") int productId, @RequestParam("code") String code) {
        DiscountResponse discountResponse = discountService.getDiscount(productId, code);

        return ResponseEntity.ok().body(
                DiscountResponseDto.builder()
                        .code(code)
                        .newPrice(discountResponse.getNewPrice())
                        .oldPrice(discountResponse.getOldPrice())
                        .build()
        );
    }


}
