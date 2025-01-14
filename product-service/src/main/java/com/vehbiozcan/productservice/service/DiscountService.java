package com.vehbiozcan.productservice.service;

import com.vehbiozcan.grpc.DiscountRequest;
import com.vehbiozcan.grpc.DiscountResponse;
import com.vehbiozcan.grpc.DiscountServiceGrpc;
import com.vehbiozcan.productservice.model.Product;
import com.vehbiozcan.productservice.service.grpc.impl.DiscountGrpcServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountService {

    private final ProductService productService;
    private final DiscountGrpcServiceImpl discountGrpcService;

    /// Bu servis içinde discount için yazdığımız grpc servisini kullanarak indirimi grpc aracılığı ile discount-service hesaplatıp
    /// geriye response olarak dönen servisimizi yazdık.

    public DiscountResponse getDiscount(int productId, String code){
        Product product = productService.getProductById(productId);
        DiscountRequest discountRequest = DiscountRequest.newBuilder()
                .setCode(code)
                .setPrice(product.getPrice().floatValue())
                .setExternalCategoryId(product.getCategory().getId())
                .build();
        return discountGrpcService.getDiscount(discountRequest);
    }

}
