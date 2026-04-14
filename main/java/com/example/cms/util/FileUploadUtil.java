package com.example.cms.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class FileUploadUtil {

    @Value("${app.upload.dir:uploads/papers}")
    private String uploadDir;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10 MB

    /**
     * Upload a PDF file and return its saved path
     */
    public String uploadFile(MultipartFile file, Long userId) throws IOException {
        validateFile(file);

        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = getExtension(originalFilename);
        String newFilename = "paper_" + userId + "_" + UUID.randomUUID().toString() + "." + extension;

        Path targetLocation = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uploadDir + "/" + newFilename;
    }

    /**
     * Delete a file by its path
     */
    public void deleteFile(String filePath) {
        if (filePath == null || filePath.isBlank()) return;
        try {
            Path path = Paths.get(filePath).toAbsolutePath().normalize();
            Files.deleteIfExists(path);
        } catch (IOException e) {
            // Log but do not throw
            System.err.println("Warning: Could not delete file: " + filePath);
        }
    }

    /**
     * Validate the uploaded file
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds the 10MB limit");
        }

        String contentType = file.getContentType();
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }
    }

    /**
     * Get file extension
     */
    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "pdf";
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
