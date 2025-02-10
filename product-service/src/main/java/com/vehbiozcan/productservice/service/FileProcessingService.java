package com.vehbiozcan.productservice.service;
import com.google.protobuf.ByteString;
import com.vehbiozcan.grpc.DiscountServiceGrpc;
import com.vehbiozcan.grpc.FileChunk;
import com.vehbiozcan.grpc.UploadStatus;
import com.vehbiozcan.productservice.model.FileUploadTask;
import io.grpc.stub.StreamObserver;
import java.io.*;
import java.util.*;
import java.util.concurrent.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile; // Spring ortamı kullanılıyorsa


public class FileProcessingService {

    // Thread-safe dosya kuyruğu
    private final BlockingQueue<FileUploadTask> fileQueue = new LinkedBlockingQueue<>();
    // Kuyruk işlemesi için scheduler
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // gRPC stub (proto’dan generate edilmiş stub’unuz)
    private final DiscountServiceGrpc.DiscountServiceStub discountAsyncStub;
    // Temp dosyaların kaydedileceği dizin
    private final File tempDir = new File("D:/temp/");

    public FileProcessingService(DiscountServiceGrpc.DiscountServiceStub discountAsyncStub) {
        this.discountAsyncStub = discountAsyncStub;
        if (!tempDir.exists()) {
            tempDir.mkdirs();
        }
        startQueueProcessor();
    }

    /**
     * Gelen MultipartFile'ı D:/temp/ dizinine kaydeder ve dosya bilgilerini kuyruğa ekler.
     */
    public void processIncomingFile(MultipartFile multipartFile) {
        // Benzersiz dosya adı oluşturuluyor
        String uniqueFileName = UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
        File tempFile = new File(tempDir, uniqueFileName);

        try (InputStream in = multipartFile.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            throw new RuntimeException("Temp dosya kaydedilirken hata oluştu", e);
        }

        // Dosyanın boyut bilgisini kullanarak görev oluşturuluyor.
        FileUploadTask task = new FileUploadTask(tempFile, tempFile.length());
        fileQueue.add(task);
        System.out.println("Dosya kuyruğa eklendi: " + tempFile.getName());
    }

    /**
     * Kuyruk işlemesini başlatır; her 500 ms’de kuyruktan maksimum 20 dosya çekilip gRPC ile gönderilir.
     */
    private void startQueueProcessor() {
        scheduler.scheduleAtFixedRate(() -> {
            try {
                List<FileUploadTask> tasks = new ArrayList<>();
                // Kuyruktan en fazla 20 görev alınır
                fileQueue.drainTo(tasks, 20);
                if (!tasks.isEmpty()) {
                    System.out.println("Kuyruktan " + tasks.size() + " dosya çekiliyor...");
                }
                // Her dosya sırayla gönderilir.
                for (FileUploadTask task : tasks) {
                    uploadFileViaGrpc(task);
                }
            } catch (Exception e) {
                System.err.println("Kuyruk işlenirken hata: " + e.getMessage());
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    /**
     * Belirtilen dosyayı gRPC üzerinden sunucuya aktarır.
     */
    private void uploadFileViaGrpc(FileUploadTask task) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                System.out.println("Sunucu cevabı (" + task.getFile().getName() + "): " + value.getMessage());
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Hata (" + task.getFile().getName() + "): " + t.getMessage());
                statusResponse[0] = UploadStatus.newBuilder()
                        .setSuccess(false)
                        .setMessage("Upload failed: " + t.getMessage())
                        .build();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                System.out.println("Dosya aktarımı tamamlandı: " + task.getFile().getName());
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadAsyncFile(responseObserver);

        try (InputStream is = new FileInputStream(task.getFile())) {
            byte[] buffer = new byte[128 * 1024]; // 128 KB’lık parçalar
            int bytesRead;
            boolean isFirst = true;

            while ((bytesRead = is.read(buffer)) != -1) {
                FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead));

                if (isFirst) {
                    // İlk parçaya dosya adını ekliyoruz (gerekirse diğer meta veriler de)
                    chunkBuilder.setFileName(task.getFile().getName())
                            .setIsFirst(true);
                    isFirst = false;
                }

                requestObserver.onNext(chunkBuilder.build());
            }
        } catch (IOException e) {
            requestObserver.onError(e);
            System.err.println("Dosya okuma hatası (" + task.getFile().getName() + "): " + e.getMessage());
        }

        requestObserver.onCompleted();

        // Sunucunun tamamlanması için bekleniyor (maksimum 2 dakika)
        try {
            if (!finishLatch.await(2, TimeUnit.MINUTES)) {
                System.err.println("Dosya aktarım süresi aşıldı: " + task.getFile().getName());
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Dosya aktarımı iptal edildi: " + task.getFile().getName());
        }
    }
}
