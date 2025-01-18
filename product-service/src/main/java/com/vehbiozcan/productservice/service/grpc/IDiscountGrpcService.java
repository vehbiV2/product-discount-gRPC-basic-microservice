package com.vehbiozcan.productservice.service.grpc;

import com.vehbiozcan.grpc.DiscountListResponse;
import com.vehbiozcan.grpc.DiscountRequest;
import com.vehbiozcan.grpc.DiscountResponse;

public interface IDiscountGrpcService {

    public DiscountResponse getDiscount(DiscountRequest discountRequest);
    public DiscountListResponse getAllDiscount();
}
