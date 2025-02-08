package com.vehbiozcan.productservice.controller;

import com.vehbiozcan.grpc.UploadStatus;
import com.vehbiozcan.productservice.service.DiscountService;
import com.vehbiozcan.productservice.service.FileService;
import com.vehbiozcan.productservice.service.grpc.impl.DiscountGrpcServiceImpl;
import com.vehbiozcan.productservice.service.grpc.util.ExcelLogger;
import com.vehbiozcan.productservice.service.grpc.util.GrpcLoggerUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/files")
public class FileController {

    private final FileService fileService;
    private final GrpcLoggerUtil grpcLoggerUtil;
    private final DiscountGrpcServiceImpl discountGrpcService;
    @Autowired
    private ExcelLogger excelLogger;

    // Yeni: Dosya upload endpoint'i
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("thread") int thread,
                                             @RequestParam("rampUp") int rampUp,
                                             @RequestParam("fileSize") int fileSize) {

        long startEpoch = System.currentTimeMillis();
        UploadStatus status = fileService.uploadFile(file);
        long endEpoch = System.currentTimeMillis();
        long duration = endEpoch - startEpoch;
        if (status.getSuccess()) {
            grpcLoggerUtil.log(duration);
            excelLogger.log(duration,fileSize,thread,rampUp);
            return ResponseEntity.ok(status.getMessage());
        } else {
            grpcLoggerUtil.log(-1);
            excelLogger.log(-1,fileSize,thread,rampUp);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(status.getMessage());
        }
    }


    @PostMapping("/upload-async")
    public ResponseEntity<String> uploadFileAsync(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("thread") int thread,
                                                  @RequestParam("rampUp") int rampUp,
                                                  @RequestParam("fileSize") int fileSize) throws InterruptedException {
        long startEpoch = System.currentTimeMillis();
        UploadStatus status = discountGrpcService.uploadAsyncFile(file);
        long endEpoch = System.currentTimeMillis();
        long duration = endEpoch - startEpoch;
        if (status.getSuccess()) {
            grpcLoggerUtil.log(duration);
            excelLogger.log(duration,fileSize,thread,rampUp);
            return ResponseEntity.ok(status.getMessage());
        } else {
            grpcLoggerUtil.log(-1);
            excelLogger.log(duration,fileSize,thread,rampUp);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(status.getMessage());
        }
    }


}
