package io.joshuasalcedo.commonlibs.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for accessing application logs.
 * Provides endpoints to download, view, or stream log files.
 */
@RestController
@RequestMapping("/api/logs")
public class LogsController {

    @Value("${LOG_FILE_PATH:${user.home}/.app/logs}")
    private String logFilePath;
    
    @Value("${spring.application.name:application}")
    private String appName;

    /**
     * Get a list of available log files
     * @return List of log files with their details
     */
    @GetMapping("/list")
    public ResponseEntity<List<LogFileInfo>> listLogFiles() {
        File logDir = new File(logFilePath);
        
        if (!logDir.exists() || !logDir.isDirectory()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ArrayList<>());
        }
        
        File[] files = logDir.listFiles((dir, name) -> name.contains(appName) && name.endsWith(".log"));
        
        if (files == null || files.length == 0) {
            return ResponseEntity.ok(new ArrayList<>());
        }
        
        List<LogFileInfo> logFiles = new ArrayList<>();
        for (File file : files) {
            logFiles.add(new LogFileInfo(
                    file.getName(),
                    file.length(),
                    file.lastModified()
            ));
        }
        
        return ResponseEntity.ok(logFiles);
    }

    /**
     * Download the current log file
     * @return Log file as a downloadable resource
     */
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile() {
        File logFile = new File(logFilePath, appName + ".log");
        
        if (!logFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        return ResponseEntity
                .ok()
                .contentLength(logFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header("Content-Disposition", "attachment; filename=\"" + logFile.getName() + "\"")
                .body(new FileSystemResource(logFile));
    }

    /**
     * Stream log file content (useful for large files)
     * @return Streaming response with log content
     */
    @GetMapping(value = "/stream", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<StreamingResponseBody> streamLogFile() {
        File logFile = new File(logFilePath, appName + ".log");
        
        if (!logFile.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        StreamingResponseBody responseBody = outputStream -> {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    outputStream.write((line + "\n").getBytes());
                    outputStream.flush();
                }
            }
        };
        
        return ResponseEntity
                .ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }

    /**
     * View the last N lines of the log file
     * @return Last lines of the log file
     */
    @GetMapping("/tail")
    public ResponseEntity<List<String>> tailLogFile() {
        Path path = Paths.get(logFilePath, appName + ".log");
        
        if (!Files.exists(path)) {
            return ResponseEntity.notFound().build();
        }
        
        try {
            // Read the last 100 lines (or fewer if file is smaller)
            List<String> lines = Files.lines(path)
                    .collect(Collectors.toCollection(ArrayList::new));
            
            int startIndex = Math.max(0, lines.size() - 100);
            return ResponseEntity.ok(lines.subList(startIndex, lines.size()));
            
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Data class for log file information
     */
    public static class LogFileInfo {
        private final String name;
        private final long size;
        private final long lastModified;

        public LogFileInfo(String name, long size, long lastModified) {
            this.name = name;
            this.size = size;
            this.lastModified = lastModified;
        }

        public String getName() {
            return name;
        }

        public long getSize() {
            return size;
        }

        public long getLastModified() {
            return lastModified;
        }
    }
}