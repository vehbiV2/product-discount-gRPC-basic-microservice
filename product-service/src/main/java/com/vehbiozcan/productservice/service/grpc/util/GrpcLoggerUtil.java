package com.vehbiozcan.productservice.service.grpc.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import com.sun.management.OperatingSystemMXBean;

@Component
public class GrpcLoggerUtil {

    @Value("${grpc.log.file:D:/grpcLogs/grpcLog.txt}")
    private String logFilePath;

    private static final Logger logger = LoggerFactory.getLogger(GrpcLoggerUtil.class);

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Türkçe formatlı ondalık sayı formatlayıcı (virgül ayraçlı, 2 basamaklı)
    private static final DecimalFormatSymbols SYMBOLS = new DecimalFormatSymbols(Locale.forLanguageTag("tr-TR"));
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("0.00", SYMBOLS);

    @Async
    public void log(long duration) {
        try {
            Path path = Paths.get(logFilePath);

            // Dosya ve dizin yoksa oluştur
            if (Files.notExists(path)) {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            }

            // Güncel tarih ve saat
            String currentDateTime = LocalDateTime.now().format(DATE_FORMATTER);
            String logMessage;

            if (duration < 0) {
                logMessage = currentDateTime + " - Error File Not Upload";
            } else {
                // JVM Heap kullanımı (MB cinsinden küsüratlı hesaplama)
                Runtime runtime = Runtime.getRuntime();
                double jvmHeapUsed = (double) (runtime.totalMemory() - runtime.freeMemory()) / (1024 * 1024);

                // CPU ve RAM Kullanımı
                OperatingSystemMXBean osBean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
                double cpuUsage = osBean.getSystemCpuLoad() * 100;

                // RAM hesaplama (GB cinsinden küsüratlı hesaplama)
                double totalPhysicalMemory = (double) osBean.getTotalPhysicalMemorySize() / (1024 * 1024 * 1024); // GB
                double freePhysicalMemory = (double) osBean.getFreePhysicalMemorySize() / (1024 * 1024 * 1024); // GB
                double usedRamGB = totalPhysicalMemory - freePhysicalMemory; // Kullanılan RAM (GB)

                // Formatlanmış log mesajı
                logMessage = String.format(
                        "%s - Duration: %d MS JVM Heap: %s MB CPU: %s%% RAM: %s GB",
                        currentDateTime,
                        duration,
                        DECIMAL_FORMAT.format(jvmHeapUsed),
                        DECIMAL_FORMAT.format(cpuUsage),
                        DECIMAL_FORMAT.format(usedRamGB)
                );
            }

            // Log dosyasına yaz
            Files.write(path,
                    (logMessage + System.lineSeparator()).getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.APPEND);

        } catch (IOException e) {
            logger.error("Log dosyasına yazılırken hata oluştu", e);
        }
    }
}

