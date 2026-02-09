package com.fangbuilt.lc_loan_system.shared.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageRequest {
    
    private int page = 1;  // 1-based index (user-facing)
    private int size = 20;
    private String sortBy = "createdAt";
    private Sort.Direction sortDirection = Sort.Direction.DESC;
    private Map<String, Object> filters = new HashMap<>();
    
    /**
     * Convert to Spring Pageable (0-based)
     */
    public Pageable toPageable() {
        return org.springframework.data.domain.PageRequest.of(
            page - 1,  // Convert to 0-based
            size,
            Sort.by(sortDirection, sortBy)
        );
    }
}
