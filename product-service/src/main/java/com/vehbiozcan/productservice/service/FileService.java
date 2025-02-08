package com.vehbiozcan.productservice.service;

import com.vehbiozcan.grpc.UploadStatus;
import com.vehbiozcan.productservice.service.grpc.impl.DiscountGrpcServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileService {

    private final DiscountGrpcServiceImpl discountGrpcService;

    public UploadStatus uploadFile(MultipartFile file) {
        try {
            return discountGrpcService.uploadFile(file);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return UploadStatus.newBuilder()
                    .setSuccess(false)
                    .setMessage("Upload interrupted: " + e.getMessage())
                    .build();
        }
    }
}
