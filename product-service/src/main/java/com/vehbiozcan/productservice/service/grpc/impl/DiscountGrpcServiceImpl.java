package com.vehbiozcan.productservice.service.grpc.impl;

import com.google.protobuf.ByteString;
import com.vehbiozcan.grpc.*;
import com.vehbiozcan.productservice.service.grpc.IDiscountGrpcService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class DiscountGrpcServiceImpl implements IDiscountGrpcService {


    // Grpc kanalı oluşturmaya yarayan nesne bizim karşı servisle bir kanal üzerinden bağlanmamızı sağlar
    private final ManagedChannel channel;

    // istek tamamlanıncaya kadar bekleyen stubdır.
    private final DiscountServiceGrpc.DiscountServiceBlockingStub discountStub;
    // Asenkron stub, stream işlemleri için kullanılacak
    private final DiscountServiceGrpc.DiscountServiceStub discountAsyncStub;

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
        this.discountAsyncStub = DiscountServiceGrpc.newStub(channel);
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

    // Yeni: Dosya upload metodunu ekliyoruz
    public UploadStatus uploadFile(MultipartFile file) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        // Response observer: sunucudan gelen sonucu yakalıyoruz.
        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        // Sunucuya göndermek üzere request observer'ı oluşturuyoruz.
        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadFile(responseObserver);

        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[64 * 1024]; // 64KB'lık parça
            int bytesRead;
            boolean isFirst = true;
            while ((bytesRead = is.read(buffer)) != -1) {
                FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead));
                if (isFirst) {
                    chunkBuilder.setFileName(file.getOriginalFilename())
                            .setIsFirst(true);
                    isFirst = false;
                }
                requestObserver.onNext(chunkBuilder.build());
            }
        } catch (IOException e) {
            requestObserver.onError(e);
        }
        // Dosya gönderiminin tamamlandığını bildiriyoruz.
        requestObserver.onCompleted();

        // Sonucun gelmesi için bekliyoruz (örn. 1 dakika)
        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload timed out");
        }
        return statusResponse[0];
    }



    /*public UploadStatus uploadAsyncFile(MultipartFile file) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        // Response observer: sunucudan gelen sonucu yakalıyoruz.
        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                System.out.println("Server Response: " + value.getMessage());
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                statusResponse[0] = UploadStatus.newBuilder().setSuccess(false).setMessage("Upload failed: " + t.getMessage()).build();
                finishLatch.countDown();

            }

            @Override
            public void onCompleted() {
                System.out.println("File upload completed.");
                finishLatch.countDown();

            }
        };

        // Sunucuya göndermek üzere request observer'ı oluşturuyoruz.
        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadAsyncFile(responseObserver);


            try (InputStream is = file.getInputStream()) {
                byte[] buffer = new byte[64 * 1024]; // 64KB'lık parça
                int bytesRead;
                boolean isFirst = true;
                while ((bytesRead = is.read(buffer)) != -1) {
                    FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                            .setContent(ByteString.copyFrom(buffer, 0, bytesRead));
                    if (isFirst) {
                        chunkBuilder.setFileName(file.getOriginalFilename())
                                .setIsFirst(true);
                        isFirst = false;
                    }
                    requestObserver.onNext(chunkBuilder.build());
                    Thread.sleep(10);
                }
            } catch (IOException e) {
                requestObserver.onError(e);
            }
            // Dosya gönderiminin tamamlandığını bildiriyoruz.
            requestObserver.onCompleted();
        // Sonucun gelmesi için bekliyoruz (örn. 1 dakika)
        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload timed out");
        }
        return statusResponse[0];
    }
*/

    public UploadStatus uploadAsyncFile(MultipartFile file) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                System.out.println("Server Response: " + value.getMessage());
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                statusResponse[0] = UploadStatus.newBuilder()
                        .setSuccess(false)
                        .setMessage("Upload failed: " + t.getMessage())
                        .build();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File upload completed.");
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadAsyncFile(responseObserver);

        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[128 * 1024];
            int bytesRead;
            boolean isFirst = true;

            while ((bytesRead = is.read(buffer)) != -1) {
                FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead));

                if (isFirst) {
                    chunkBuilder.setFileName(file.getOriginalFilename())
                            .setIsFirst(true);
                    isFirst = false;
                }
                requestObserver.onNext(chunkBuilder.build());
                Thread.sleep(10);
            }
        } catch (IOException e) {
            requestObserver.onError(e);
            throw new RuntimeException("File reading error", e);
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload timed out");
        }
        return statusResponse[0];
    }


    public UploadStatus uploadAsyncFileQueue(MultipartFile file) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                System.out.println("Server Response: " + value.getMessage());
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Error: " + t.getMessage());
                statusResponse[0] = UploadStatus.newBuilder()
                        .setSuccess(false)
                        .setMessage("Upload failed: " + t.getMessage())
                        .build();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("File upload completed.");
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadAsyncFile(responseObserver);

        try (InputStream is = file.getInputStream()) {
            byte[] buffer = new byte[128 * 1024];
            int bytesRead;
            boolean isFirst = true;

            while ((bytesRead = is.read(buffer)) != -1) {
                FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead));

                if (isFirst) {
                    chunkBuilder.setFileName(file.getOriginalFilename())
                            .setIsFirst(true);
                    isFirst = false;
                }
                requestObserver.onNext(chunkBuilder.build());
                Thread.sleep(10);
            }
        } catch (IOException e) {
            requestObserver.onError(e);
            throw new RuntimeException("File reading error", e);
        }

        requestObserver.onCompleted();

        if (!finishLatch.await(2, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload timed out");
        }
        return statusResponse[0];
    }

}
