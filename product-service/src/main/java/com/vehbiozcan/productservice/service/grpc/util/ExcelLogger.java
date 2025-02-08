package com.vehbiozcan.productservice.service.grpc.util;
/*

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ExcelLogger {

    private static final String FILE_PATH = "D:/grpcLogs/grpcLog.xlsx";

    // Decimal format for two decimal places
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    @Async
    public void log(long duration) {
        try {
            writeLog(duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(long duration) throws IOException {
        File file = new File(FILE_PATH);
        Workbook workbook;
        Sheet sheet;

        if (!file.exists()) {
            Files.createDirectories(Paths.get("D:/grpcLogs/"));
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Logs");
            createHeader(sheet);
        } else {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }

        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);

        // Write the log data
        row.createCell(0).setCellValue(getCurrentTimestamp());
        row.createCell(1).setCellValue(duration < 0 ? -1 : duration); // Duration in ms
        row.createCell(2).setCellValue(DECIMAL_FORMAT.format(getJvmHeap()) + " MB");
        row.createCell(3).setCellValue(DECIMAL_FORMAT.format(getCpuUsage()) + " %");
        row.createCell(4).setCellValue(DECIMAL_FORMAT.format(getRamUsage()) + " GB");

        // Save the file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Duration (ms)");
        header.createCell(2).setCellValue("JVM Heap (MB)");
        header.createCell(3).setCellValue("CPU (%)");
        header.createCell(4).setCellValue("RAM (GB)");

        // Bold headers
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < 5; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private double getJvmHeap() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024);
    }

    private double getCpuUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100;
    }

    private double getRamUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / (1024.0 * 1024 * 1024); // GB
    }
}
*/
/*

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ExcelLogger {

    private static final String FILE_PATH = "D:/grpcLogs/grpcLog.xlsx";

    // Decimal format for two decimal places
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    @Async
    public void log(long duration) {
        try {
            writeLog(duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(long duration) throws IOException {
        File file = new File(FILE_PATH);
        Workbook workbook;
        Sheet sheet;

        if (!file.exists()) {
            Files.createDirectories(Paths.get("D:/grpcLogs/"));
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Logs");
            createHeader(sheet);
        } else {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }

        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);

        // Write the log data (numeric values without units)
        row.createCell(0).setCellValue(getCurrentTimestamp());
        row.createCell(1).setCellValue(duration < 0 ? -1 : duration); // Duration in ms
        row.createCell(2).setCellValue(DECIMAL_FORMAT.format(getJvmHeap())); // JVM Heap in MB (numeric value only)
        row.createCell(3).setCellValue(DECIMAL_FORMAT.format(getCpuUsage())); // CPU usage in % (numeric value only)
        row.createCell(4).setCellValue(DECIMAL_FORMAT.format(getRamUsage())); // RAM usage in GB (numeric value only)

        // Save the file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Duration (ms)");
        header.createCell(2).setCellValue("JVM Heap (MB)");
        header.createCell(3).setCellValue("CPU (%)");
        header.createCell(4).setCellValue("RAM (GB)");

        // Bold headers
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < 5; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private double getJvmHeap() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024); // In MB
    }

    private double getCpuUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100; // CPU in percentage
    }

    private double getRamUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / (1024.0 * 1024 * 1024); // RAM in GB
    }
}
*/

/*
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class ExcelLogger {

    private static final String FILE_PATH = "D:/grpcLogs/grpcLog.xlsx";

    // Decimal format for two decimal places
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    @Async
    public void log(long duration) {
        try {
            writeLog(duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(long duration) throws IOException {
        File file = new File(FILE_PATH);
        Workbook workbook;
        Sheet sheet;

        if (!file.exists()) {
            Files.createDirectories(Paths.get("D:/grpcLogs/"));
            workbook = new XSSFWorkbook();
            sheet = workbook.createSheet("Logs");
            createHeader(sheet);
        } else {
            FileInputStream fis = new FileInputStream(file);
            workbook = new XSSFWorkbook(fis);
            sheet = workbook.getSheetAt(0);
            fis.close();
        }

        int lastRow = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(lastRow);

        // Write the log data (numeric values without units)
        row.createCell(0).setCellValue(getCurrentTimestamp());
        row.createCell(1).setCellValue(duration < 0 ? -1 : duration); // Duration in ms
        row.createCell(2).setCellValue(getJvmHeap()); // JVM Heap in MB (numeric value only)
        row.createCell(3).setCellValue(getCpuUsage()); // CPU usage in % (numeric value only)
        row.createCell(4).setCellValue(getRamUsage()); // RAM usage in GB (numeric value only)

        // Save the file
        FileOutputStream fos = new FileOutputStream(file);
        workbook.write(fos);
        fos.close();
        workbook.close();
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Duration (ms)");
        header.createCell(2).setCellValue("JVM Heap (MB)");
        header.createCell(3).setCellValue("CPU (%)");
        header.createCell(4).setCellValue("RAM (GB)");

        // Bold headers
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < 5; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }

        // Set column width for better readability
        sheet.setColumnWidth(0, 3000); // Date column
        sheet.setColumnWidth(1, 5000); // Duration column
        sheet.setColumnWidth(2, 5000); // JVM Heap column
        sheet.setColumnWidth(3, 5000); // CPU column
        sheet.setColumnWidth(4, 5000); // RAM column
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private double getJvmHeap() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024); // In MB
    }

    private double getCpuUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100; // CPU in percentage
    }

    private double getRamUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / (1024.0 * 1024 * 1024); // RAM in GB
    }
}*/
/*
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ExcelLogger {

    private static final String FILE_PATH = "D:/grpcLogs/grpcLog.xlsx";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    // Lock to synchronize file access
    private final Lock fileLock = new ReentrantLock();

    @Async
    public void log(long duration) {
        try {
            writeLog(duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(long duration) throws IOException {
        fileLock.lock();  // Lock the file access to avoid concurrent writing
        try {
            File file = new File(FILE_PATH);
            Workbook workbook;
            Sheet sheet;

            // Check if file exists, if not create it
            if (!file.exists()) {
                Files.createDirectories(Paths.get("D:/grpcLogs/"));
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Logs");
                createHeader(sheet);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                }
            }

            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);

            // Write the log data (numeric values without units)
            row.createCell(0).setCellValue(getCurrentTimestamp());
            row.createCell(1).setCellValue(duration < 0 ? -1 : duration); // Duration in ms
            row.createCell(2).setCellValue(getJvmHeap()); // JVM Heap in MB (numeric value only)
            row.createCell(3).setCellValue(getCpuUsage()); // CPU usage in % (numeric value only)
            row.createCell(4).setCellValue(getRamUsage()); // RAM usage in GB (numeric value only)

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();
        } finally {
            fileLock.unlock();  // Unlock after writing is complete
        }
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Duration (ms)");
        header.createCell(2).setCellValue("JVM Heap (MB)");
        header.createCell(3).setCellValue("CPU (%)");
        header.createCell(4).setCellValue("RAM (GB)");

        // Bold headers
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < 5; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }

        // Set column width for better readability
        sheet.setColumnWidth(0, 3000); // Date column
        sheet.setColumnWidth(1, 5000); // Duration column
        sheet.setColumnWidth(2, 5000); // JVM Heap column
        sheet.setColumnWidth(3, 5000); // CPU column
        sheet.setColumnWidth(4, 5000); // RAM column
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private double getJvmHeap() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024); // In MB
    }

    private double getCpuUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100; // CPU in percentage
    }

    private double getRamUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / (1024.0 * 1024 * 1024); // RAM in GB
    }
}*/


import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class ExcelLogger {

    private static final String FILE_PATH = "D:/grpcLogs/grpcLog.xlsx";
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#0.00");

    // Lock to synchronize file access
    private final Lock fileLock = new ReentrantLock();

    @Async
    public void log(long duration) {
        try {
            writeLog(duration);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeLog(long duration) throws IOException {
        fileLock.lock();  // Lock the file access to avoid concurrent writing
        try {
            File file = new File(FILE_PATH);
            Workbook workbook;
            Sheet sheet;

            // Check if file exists, if not create it
            if (!file.exists()) {
                Files.createDirectories(Paths.get("D:/grpcLogs/"));
                workbook = new XSSFWorkbook();
                sheet = workbook.createSheet("Logs");
                createHeader(sheet);
            } else {
                try (FileInputStream fis = new FileInputStream(file)) {
                    workbook = new XSSFWorkbook(fis);
                    sheet = workbook.getSheetAt(0);
                }
            }

            int lastRow = sheet.getLastRowNum() + 1;
            Row row = sheet.createRow(lastRow);

            // Write the log data (numeric values without units)
            row.createCell(0).setCellValue(getCurrentTimestamp());
            row.createCell(1).setCellValue(duration < 0 ? -1 : duration); // Duration in ms
            row.createCell(2).setCellValue(getJvmHeap()); // JVM Heap in MB (numeric value only)
            row.createCell(3).setCellValue(getCpuUsage()); // CPU usage in % (numeric value only)
            row.createCell(4).setCellValue(getRamUsage()); // RAM usage in GB (numeric value only)

            // Save the file
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
            workbook.close();
        } finally {
            fileLock.unlock();  // Unlock after writing is complete
        }
    }

    private void createHeader(Sheet sheet) {
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("Date");
        header.createCell(1).setCellValue("Duration (ms)");
        header.createCell(2).setCellValue("JVM Heap (MB)");
        header.createCell(3).setCellValue("CPU (%)");
        header.createCell(4).setCellValue("RAM (GB)");

        // Bold headers
        CellStyle headerStyle = sheet.getWorkbook().createCellStyle();
        Font font = sheet.getWorkbook().createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        for (int i = 0; i < 5; i++) {
            header.getCell(i).setCellStyle(headerStyle);
        }

        // Set column width for better readability
        sheet.setColumnWidth(0, 3000); // Date column
        sheet.setColumnWidth(1, 5000); // Duration column
        sheet.setColumnWidth(2, 5000); // JVM Heap column
        sheet.setColumnWidth(3, 5000); // CPU column
        sheet.setColumnWidth(4, 5000); // RAM column
    }

    private String getCurrentTimestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    private double getJvmHeap() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024.0 * 1024); // In MB
    }

    private double getCpuUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return osBean.getSystemCpuLoad() * 100; // CPU in percentage
    }

    private double getRamUsage() {
        com.sun.management.OperatingSystemMXBean osBean =
                (com.sun.management.OperatingSystemMXBean) java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        return (osBean.getTotalPhysicalMemorySize() - osBean.getFreePhysicalMemorySize()) / (1024.0 * 1024 * 1024); // RAM in GB
    }
}