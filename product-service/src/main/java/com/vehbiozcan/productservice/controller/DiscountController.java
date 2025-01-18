package com.vehbiozcan.productservice.controller;

import com.vehbiozcan.grpc.DiscountListResponse;
import com.vehbiozcan.grpc.DiscountResponse;
import com.vehbiozcan.grpc.DiscountType;
import com.vehbiozcan.productservice.model.dto.DiscountDto;
import com.vehbiozcan.productservice.model.dto.DiscountListResponseDto;
import com.vehbiozcan.productservice.model.dto.DiscountResponseDto;
import com.vehbiozcan.productservice.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

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

    @GetMapping("/all")
    public ResponseEntity<DiscountListResponseDto> getAllDiscount(){
        DiscountListResponse discountListResponse = discountService.getAllDiscount();
        List<DiscountType> discountsList = discountListResponse.getDiscountsList();
        List<DiscountDto> discountDtoList = discountsList.stream().map(discountType -> DiscountDto.builder()
                .code(discountType.getCode())
                .discount(discountType.getDiscountPrice())
                .id((int)discountType.getId()).build()
        ).collect(Collectors.toList());
        DiscountListResponseDto discountListResponseDto = new DiscountListResponseDto();
        discountListResponseDto.setDiscountDtoList(discountDtoList);
        return ResponseEntity.ok().body(discountListResponseDto);
    }


}
