package com.fangbuilt.lc_loan_system.shared.service;

import com.fangbuilt.lc_loan_system.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditScoreService {
    
    private final RestTemplate restTemplate;
    private static final String CREDIT_SCORE_API = 
        "https://www.randomnumberapi.com/api/v1.0/random?min=300&max=850&count=1";
    
    /**
     * Get credit score from external API
     * Returns a score between 300-850
     */
    public int getCreditScore(String customerId) {
        try {
            log.info("Fetching credit score for customer: {}", customerId);
            
            Integer[] response = restTemplate.getForObject(CREDIT_SCORE_API, Integer[].class);
            
            if (response == null || response.length == 0) {
                throw new BadRequestException("Credit score service returned empty response");
            }
            
            int score = response[0];
            log.info("Credit score for customer {}: {}", customerId, score);
            
            return score;
            
        } catch (RestClientException e) {
            log.error("Failed to fetch credit score: {}", e.getMessage());
            throw new BadRequestException("Credit score service unavailable. Please try again later.");
        }
    }
    
    /**
     * Determine loan eligibility based on credit score
     * 
     * Rules:
     * - Score >= 700: Approved with 10% interest
     * - Score 600-699: Approved with 15% interest  
     * - Score < 600: Rejected
     */
    public LoanEligibility checkEligibility(int creditScore) {
        if (creditScore >= 700) {
            return new LoanEligibility(true, 10.0, "Excellent credit score");
        } else if (creditScore >= 600) {
            return new LoanEligibility(true, 15.0, "Good credit score");
        } else {
            return new LoanEligibility(false, 0.0, "Credit score too low");
        }
    }
    
    public record LoanEligibility(
        boolean approved,
        double interestRate,
        String reason
    ) {}
}
