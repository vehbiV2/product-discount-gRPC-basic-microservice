package com.vehbiozcan.discountservice.service;

import com.vehbiozcan.discountservice.entity.Category;
import com.vehbiozcan.discountservice.entity.Discount;
import com.vehbiozcan.discountservice.repository.CategoryRepository;
import com.vehbiozcan.discountservice.repository.DiscountRepository;
import com.vehbiozcan.grpc.*;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.beans.factory.annotation.Value;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class DiscountGrpcService extends DiscountServiceGrpc.DiscountServiceImplBase {

    private final DiscountRepository discountRepository;
    private final CategoryRepository categoryRepository;

    @Value("${file.upload_dir}")
    private String uploadDir;

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

    @Override
    public StreamObserver<FileChunk> uploadFile(StreamObserver<UploadStatus> responseObserver) {
        return new StreamObserver<FileChunk>() {

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            String fileName = "uploaded_file.pdf"; // default değer, ilk chunk'ta güncellenecek

            @Override
            public void onNext(FileChunk chunk) {
                // İlk chunk'ta dosya adını da gönderiyoruz
                if (chunk.getIsFirst()) {
                    fileName = chunk.getFileName();
                    log.info("Receiving file: {}", fileName);
                }
                try {
                    bos.write(chunk.getContent().toByteArray());
                } catch (IOException e) {
                    log.error("Error writing file chunk", e);
                    responseObserver.onError(e);
                }
            }

            @Override
            public void onError(Throwable t) {
                log.error("Error receiving file", t);
            }

            @Override
            public void onCompleted() {
                try {
                    String uploadFileName =  "grpc_" + UUID.randomUUID().toString() + "_" + fileName;
                    // Dosyayı bir dizine kaydediyoruz. (Dizin var mı kontrol edin!)
                    File outputFile = new File(uploadDir + File.separator + uploadFileName);
                    // Eğer klasör yoksa oluşturabilirsiniz:
                    outputFile.getParentFile().mkdirs();
                    Files.write(outputFile.toPath(), bos.toByteArray());
                    log.info("File {} saved successfully", uploadFileName);

                    UploadStatus status = UploadStatus.newBuilder()
                            .setSuccess(true)
                            .setMessage("File uploaded successfully")
                            .build();
                    responseObserver.onNext(status);
                } catch (IOException e) {
                    log.error("Error saving file", e);
                    UploadStatus status = UploadStatus.newBuilder()
                            .setSuccess(false)
                            .setMessage("Failed to save file: " + e.getMessage())
                            .build();
                    responseObserver.onNext(status);
                } finally {
                    responseObserver.onCompleted();
                }
            }
        };
    }

}

