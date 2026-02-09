package com.fangbuilt.lc_loan_system.shared.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {
    
    private String uploadDir = "uploads";
    private long maxSize = 2097152;  // 2MB
    private String[] allowedTypes = {"image/jpeg", "image/png", "application/pdf"};
}
