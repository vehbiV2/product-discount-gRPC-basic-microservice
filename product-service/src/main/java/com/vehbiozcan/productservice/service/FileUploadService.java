package com.vehbiozcan.productservice.service;

import com.vehbiozcan.grpc.FileChunk;
import com.vehbiozcan.grpc.UploadStatus;
import io.grpc.stub.StreamObserver;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FileUploadService {
    /*// TEMP Dir Kullanılıyor

    public UploadStatus uploadAsyncFile(MultipartFile file) throws InterruptedException {
        final CountDownLatch finishLatch = new CountDownLatch(1);
        final UploadStatus[] statusResponse = new UploadStatus[1];

        StreamObserver<UploadStatus> responseObserver = new StreamObserver<>() {
            @Override
            public void onNext(UploadStatus value) {
                statusResponse[0] = value;
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Upload failed: " + t.getMessage());
                finishLatch.countDown();
            }

            @Override
            public void onCompleted() {
                finishLatch.countDown();
            }
        };

        StreamObserver<FileChunk> requestObserver = discountAsyncStub.uploadFile(responseObserver);

        // Büyük dosya upload işlemi için: Geçici dosyaya kaydet ve disk üzerinden oku.
        File tempFile = null;
        try {
            // Geçici dosya oluşturuluyor
            tempFile = File.createTempFile("upload_", ".tmp");
            file.transferTo(tempFile);  // Dosya geçici olarak diske aktarılıyor.

            try (InputStream is = new FileInputStream(tempFile)) {
                byte[] buffer = new byte[16 * 1024]; // 16KB'lık buffer kullanılıyor.
                int bytesRead;
                boolean isFirst = true;
                while ((bytesRead = is.read(buffer)) != -1) {
                    FileChunk.Builder chunkBuilder = FileChunk.newBuilder()
                            .setContent(ByteString.copyFrom(buffer, 0, bytesRead));
                    if (isFirst) {
                        chunkBuilder.setFileName(file.getOriginalFilename()).setIsFirst(true);
                        isFirst = false;
                    }
                    requestObserver.onNext(chunkBuilder.build());
                }
                requestObserver.onCompleted();
            }
        } catch (IOException e) {
            requestObserver.onError(e);
            return UploadStatus.newBuilder()
                    .setStatus("FAILED")
                    .setMessage(e.getMessage())
                    .build();
        } finally {
            if (tempFile != null && tempFile.exists() && !tempFile.delete()) {
                System.err.println("Geçici dosya silinemedi: " + tempFile.getAbsolutePath());
            }
        }

        if (!finishLatch.await(1, TimeUnit.MINUTES)) {
            throw new RuntimeException("Upload timed out");
        }
        return statusResponse[0] != null
                ? statusResponse[0]
                : UploadStatus.newBuilder().setStatus("FAILED").setMessage("Unknown error").build();
    }
*/

}
