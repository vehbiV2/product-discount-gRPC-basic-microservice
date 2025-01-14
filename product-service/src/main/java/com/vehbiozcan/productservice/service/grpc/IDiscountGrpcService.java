package com.vehbiozcan.productservice.service.grpc;

import com.vehbiozcan.grpc.DiscountRequest;
import com.vehbiozcan.grpc.DiscountResponse;

public interface IDiscountGrpcService {

    public DiscountResponse getDiscount(DiscountRequest discountRequest);
}
