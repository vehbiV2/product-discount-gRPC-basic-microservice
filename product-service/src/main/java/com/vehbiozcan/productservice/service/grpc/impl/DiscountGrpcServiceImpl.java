package com.vehbiozcan.productservice.service.grpc.impl;

import com.google.protobuf.ByteString;
import com.vehbiozcan.grpc.*;
import com.vehbiozcan.productservice.model.FileUploadTask;
import com.vehbiozcan.productservice.model.dto.QueueItem;
import com.vehbiozcan.productservice.service.grpc.IDiscountGrpcService;
import com.vehbiozcan.productservice.service.grpc.util.ExcelLogger;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class DiscountGrpcServiceImpl implements IDiscountGrpcService {


    @Autowired
    private ExcelLogger excelLogger;
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
        startQueueProcessor();
    }

    // Thread-safe dosya kuyruğu
    private final BlockingQueue<QueueItem> fileQueue = new LinkedBlockingQueue<>();
    // Kuyruk işlemesi için scheduler
    // private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    // Temp dosyaların kaydedileceği dizin
    private final File tempDir = new File("D:/temp/");


    private void processSingleFile(QueueItem item) {
        File file = new File(item.getFilePath());
        if (file.exists()) {
            long startEpoch = System.currentTimeMillis();
            uploadFileViaGrpc(new FileUploadTask(file, file.length()));
            long endEpoch = System.currentTimeMillis();
            long duration = endEpoch - startEpoch;
            excelLogger.log(duration, item.getFileSize(), item.getThread(), item.getRampUp());
            file.delete(); // İşlenen temp dosyayı siliyoruz
        } else {
            System.err.println("Dosya bulunamadı: " + item.getFilePath());
        }
    }

    private final ExecutorService consumerExecutor = Executors.newFixedThreadPool(10);


    private void startQueueProcessor() {
        consumerExecutor.submit(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    int availableThreads = ((ThreadPoolExecutor) consumerExecutor).getMaximumPoolSize() - ((ThreadPoolExecutor) consumerExecutor).getActiveCount();
                    if (availableThreads > 0) {
                        List<QueueItem> queueItems = new ArrayList<>();
                        fileQueue.drainTo(queueItems, availableThreads);

                        if (queueItems.isEmpty()) { // Kuyrukta yeterli eleman yoksa bekle
                            Thread.sleep(100);
                            continue;
                        }

                        for (QueueItem queueItem : queueItems) {
                            consumerExecutor.submit(() -> processSingleFile(queueItem));
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    System.err.println("Dosya işlenirken hata: " + e.getMessage());
                }
            }
        });
    }


    public UploadStatus processIncomingFile(MultipartFile multipartFile, int thread, int rampUp, int fileSize) {

        String uniqueFileName = UUID.randomUUID() + "_" + multipartFile.getOriginalFilename();
        File tempFile = new File(tempDir, uniqueFileName);

        try (InputStream in = multipartFile.getInputStream();
             OutputStream out = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[64 * 1024];
            int bytesRead;
            while ((bytesRead = in.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }

            // Dosyayı kuyruğa ekle ve sonucu dön
            fileQueue.add(new QueueItem(tempFile.getAbsolutePath(), thread, rampUp, fileSize));
            System.out.println("Dosya kuyruğa eklendi: " + tempFile.getName());

            return UploadStatus.newBuilder()
                    .setSuccess(true)
                    .setMessage("Dosya kuyruğa başarıyla eklendi")
                    .build();

        } catch (IOException e) {
            throw new RuntimeException("Temp dosya kaydedilirken hata oluştu", e);
        }
    }


    private UploadStatus uploadFileViaGrpc(FileUploadTask task) {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<UploadStatus>() {
            @Override
            public void onNext(UploadStatus value) {
                System.out.println("Sunucu cevabı: " + value.getMessage());
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Hata: " + t.getMessage());
                statusResponse[0] = UploadStatus.newBuilder()
                        .setSuccess(false)
                        .setMessage("Upload failed: " + t.getMessage())
                        .build();
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadAsyncFile(responseObserver);

        try (InputStream is = new FileInputStream(task.getFile())) {
            byte[] buffer = new byte[128 * 1024];
            int bytesRead;
            boolean isFirst = true;

            while ((bytesRead = is.read(buffer)) != -1) {
                FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                        .setContent(ByteString.copyFrom(buffer, 0, bytesRead));

                if (isFirst) {
                    chunkBuilder.setFileName(task.getFile().getName()).setIsFirst(true);
                    isFirst = false;
                }
                requestObserver.onNext(chunkBuilder.build());
            }
        } catch (IOException e) {
            requestObserver.onError(e);
            throw new RuntimeException("Dosya okuma hatası", e);
        }
        requestObserver.onCompleted();

        try {
            if (!finishLatch.await(2, TimeUnit.MINUTES)) {
                System.err.println("Dosya aktarım süresi aşıldı");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Dosya aktarımı iptal edildi");
        }
        return statusResponse[0];
    }



/*--------------------------------------------------------------------------------------------------------------------*/

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
            Thread.sleep(20);
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