package com.fangbuilt.lc_loan_system.shared.service;

import com.fangbuilt.lc_loan_system.shared.config.FileStorageProperties;
import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileStorageService {
    
    private final FileStorageProperties fileStorageProperties;
    private Path fileStorageLocation;
    
    @PostConstruct
    public void init() {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir())
            .toAbsolutePath()
            .normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
            log.info("Upload directory created at: {}", this.fileStorageLocation);
        } catch (IOException ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }
    
    public String storeFile(MultipartFile file) {
        validateFile(file);
        
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String newFilename = UUID.randomUUID().toString() + fileExtension;
        
        try {
            Path targetLocation = this.fileStorageLocation.resolve(newFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
            
            log.info("File stored successfully: {}", newFilename);
            return newFilename;
            
        } catch (IOException ex) {
            throw new BadRequestException("Could not store file. Please try again!");
        }
    }
    
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return resource;
            } else {
                throw new BadRequestException("File not found: " + filename);
            }
            
        } catch (MalformedURLException ex) {
            throw new BadRequestException("File not found: " + filename);
        }
    }
    
    public void deleteFile(String filename) {
        try {
            Path filePath = this.fileStorageLocation.resolve(filename).normalize();
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", filename);
        } catch (IOException ex) {
            log.error("Could not delete file: {}", filename, ex);
        }
    }
    
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BadRequestException("File is empty");
        }
        
        if (file.getSize() > fileStorageProperties.getMaxSize()) {
            throw new BadRequestException(
                "File size exceeds maximum limit of " + 
                (fileStorageProperties.getMaxSize() / 1024 / 1024) + "MB"
            );
        }
        
        String contentType = file.getContentType();
        if (contentType == null || 
            !Arrays.asList(fileStorageProperties.getAllowedTypes()).contains(contentType)) {
            throw new BadRequestException(
                "Invalid file type. Allowed types: " + 
                String.join(", ", fileStorageProperties.getAllowedTypes())
            );
        }
        
        String filename = StringUtils.cleanPath(file.getOriginalFilename());
        if (filename.contains("..")) {
            throw new BadRequestException("Invalid filename: " + filename);
        }
    }
    
    private String getFileExtension(String filename) {
        int lastDot = filename.lastIndexOf('.');
        return lastDot > 0 ? filename.substring(lastDot) : "";
    }
}
