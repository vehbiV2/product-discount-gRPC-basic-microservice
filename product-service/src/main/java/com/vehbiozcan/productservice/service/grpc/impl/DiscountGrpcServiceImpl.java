package com.vehbiozcan.productservice.service.grpc.impl;

import com.vehbiozcan.grpc.*;
import com.vehbiozcan.productservice.service.grpc.IDiscountGrpcService;
import lombok.RequiredArgsConstructor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountGrpcServiceImpl implements IDiscountGrpcService {


    // Grpc kanalı oluşturmaya yarayan nesne bizim karşı servisle bir kanal üzerinden bağlanmamızı sağlar
    private final ManagedChannel channel;

    // istek tamamlanıncaya kadar bekleyen stubdır.
    private final DiscountServiceGrpc.DiscountServiceBlockingStub discountStub;

    @Value("${discount.grpc.host}")
    private String grpcHost;

    @Value("${discount.grpc.port}")
    private int grpcPort;

    // Bu constructor, bizim channel açmak ve gerekli ayarlamaları yapmak için gereken bilgiler ile bir channel oluşturur
    // bu bilgiler host ve port adresidir
    public DiscountGrpcServiceImpl() {
        // gRPC sunucusunun adresini ve portunu sağlıyoruz

        this.channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        // istek tamamlanıncaya kadar bekleyen stubdır.
        this.discountStub = DiscountServiceGrpc.newBlockingStub(channel);
    }

    //Bu metod bizim oluşturduğumuz proto dosyamızın compile olmasıyla gelen discount servisi ifade eder
    @Override
    public DiscountResponse getDiscount(DiscountRequest request) {
        return discountStub.getDiscount(request);

    }

    @Override
    public DiscountListResponse getAllDiscount() {
        // Boş parametreli gideceği için bu şekilde tanımladık
        return discountStub.getAllDiscount(Empty.newBuilder().build());
    }
}
