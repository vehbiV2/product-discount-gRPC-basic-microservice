package com.vehbiozcan.productservice.service.grpc.impl;

import com.vehbiozcan.grpc.DiscountRequest;
import com.vehbiozcan.grpc.DiscountResponse;
import com.vehbiozcan.grpc.DiscountServiceGrpc;
import com.vehbiozcan.productservice.service.grpc.IDiscountGrpcService;
import lombok.RequiredArgsConstructor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DiscountGrpcServiceImpl implements IDiscountGrpcService {
/*
    // istek tamamlanıncaya kadar bekleyen stubdır.
    private DiscountServiceGrpc.DiscountServiceBlockingStub discountServiceBlockingStub;

    // Grpc kanalı oluşturmaya yarayan nesne bizim karşı servisle bir kanal üzerinden bağlanmamızı sağlar
    private ManagedChannel channel;

    // 2 parametreli constructor, bu bizim channel açmak ve gerekli ayarlamaları yapmak için gereken bilgileri alır
    // bu bilgiler host  ve port adresidir
    public DiscountGrpcServiceImpl(@Value("${discount.grpc.host}") String grpcHost, @Value("${discount.grpc.port}") int grpcPort){
        System.out.println("GRPC Infos -> " + grpcHost + " : " + grpcPort);
        this.channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
    }

    //Bu metod bizim oluşturduğumuz proto dosyamızın compile olmasıyla gelen discount servisi ifade eder

    @Override
    public DiscountResponse getDiscount(DiscountRequest discountRequest) {
        discountServiceBlockingStub = DiscountServiceGrpc.newBlockingStub(this.channel);
        DiscountResponse discountResponse = discountServiceBlockingStub.getDiscount(discountRequest);

        return discountResponse;
    }*/

    private final ManagedChannel channel;
    private final DiscountServiceGrpc.DiscountServiceBlockingStub discountStub;

    public DiscountGrpcServiceImpl() {
        // gRPC sunucusunun adresini ve portunu sağlıyoruz
        this.channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        this.discountStub = DiscountServiceGrpc.newBlockingStub(channel);
    }

    @Override
    public DiscountResponse getDiscount(DiscountRequest request) {
        return discountStub.getDiscount(request);

    }
}
