package com.vehbiozcan.productservice.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DiscountResponseDto {
    private float oldPrice;
    private float newPrice;
    private String code;
}
