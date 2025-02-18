package com.vehbiozcan.discountservice.service;

import com.vehbiozcan.discountservice.entity.Category;
import com.vehbiozcan.discountservice.entity.Discount;
import com.vehbiozcan.discountservice.repository.CategoryRepository;
import com.vehbiozcan.discountservice.repository.DiscountRepository;
import com.vehbiozcan.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.server.service.GrpcService;


import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
public class DiscountGrpcService extends DiscountServiceGrpc.DiscountServiceImplBase {

    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;

    // Bu bizim proto dosyasında oluşturuduğumuz metodun java ya compile edilmiş halidir.
    @Override
    public void getDiscount(DiscountRequest request, StreamObserver<DiscountResponse> responseObserver) {
        /// Categorimizi external idmize göre çektik.
        Category category = categoryRepository.findByExternalId(String.valueOf(request.getExternalCategoryId()))
                .orElseThrow(() -> new RuntimeException("Category not found"));

        Optional<Discount> discount = discountRepository.findByCodeAndCategoryId(request.getCode(), category.getId());
        if (discount.isPresent()) {
            ///İndirimi uyguluyoruz eğer discount nesnemiz varsa. Çıkarma işlemini tersten yaptık bu yüzden de -1 ile çarptık.
            BigDecimal newPrice = discount.get().getDiscountPrice().subtract(BigDecimal.valueOf(request.getPrice())).multiply(BigDecimal.valueOf(-1));

            /// Response nesnemizi build edip responseObserver.onNext() diyerek clienta responsumuzu döndük.
            responseObserver.onNext(
                    DiscountResponse.newBuilder()
                            .setCode(discount.get().getCode())
                            .setOldPrice(request.getPrice())
                            .setNewPrice(newPrice.floatValue())
                            .setResponse(Response.newBuilder()
                                    .setStatusCode(true)
                                    .setMessage("Discount has been applied successfuly")
                                    .build())
                    .build()
            );

        }else {
            responseObserver.onNext(
                    DiscountResponse.newBuilder()
                            .setOldPrice(request.getPrice())
                            .setNewPrice(request.getPrice())
                            .setCode(discount.get().getCode())
                            .setResponse(Response.newBuilder()
                                    .setStatusCode(false)
                                    .setMessage("Code and Category does not exist")
                            .build())
                    .build()
            );

        }

        /// responseObserver.onCompleted() cliente artık işlemin tamamlandığı bilgisi verir.
        responseObserver.onCompleted();

    }

    @Override
    public void getAllDiscount(Empty request, StreamObserver<DiscountListResponse> responseObserver) {
        // Veritabanından tüm discountları aldık
        List<Discount> discountList= discountRepository.findAll();

        //Discount nesnelerimizi protoda tanımladığımız şekliyle gRPC yanıt formatına çevirdik
        // Liste formatında geldiği için liste formatına dönüştürdük
        List<DiscountType> discountTypeList = discountList.stream()
                .map(discount ->
                        DiscountType.newBuilder()
                                .setCode(discount.getCode())
                                .setDiscountPrice(discount.getDiscountPrice().floatValue())
                                .setId(discount.getId())
                        .build()).collect(Collectors.toList());

        // Sonra listeyi yine proto dosyasnda tanımladığımız grpc liste formatına dönüştürdük
        DiscountListResponse response = DiscountListResponse.newBuilder()
                .addAllDiscounts(discountTypeList)
                .build();
        // Response olarak döndük
        responseObserver.onNext(response);
        // Kanalı kapattık
        responseObserver.onCompleted();
    }
}

