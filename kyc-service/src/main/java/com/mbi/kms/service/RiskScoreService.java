package com.mbi.kms.service;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.entity.RiskLevel;
import com.mbi.kms.entity.RiskScoreConfig;
import com.mbi.kms.repository.RiskScoreConfigRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskScoreService {

    private final RiskScoreConfigRepository riskScoreConfigRepository;
    private final Random random = new Random();

    public RiskAssessmentResult calculateRiskScore(KycApplication application) {
        log.info("Calculating risk score for application: {}", application.getId());

        int totalRiskScore = 0;

        // Get risk score configurations
        List<RiskScoreConfig> configs = riskScoreConfigRepository.findAll();

        // Calculate risk based on various factors
        totalRiskScore += calculateCountryRisk(application);
        totalRiskScore += calculateDocumentTypeRisk(application);
        totalRiskScore += calculateNameComplexityRisk(application);
        totalRiskScore += calculateRandomRiskFactor(); // Small random factor for variety

        // Ensure score is within 0-100 range
        totalRiskScore = Math.min(100, Math.max(0, totalRiskScore));

        // Determine risk level based on score
        RiskLevel riskLevel = determineRiskLevel(totalRiskScore);

        application.setRiskScore(totalRiskScore);
        application.setRiskLevel(riskLevel);

        log.info("Risk score calculated: {} - Risk Level: {}", totalRiskScore, riskLevel);

        return RiskAssessmentResult.builder()
                .riskScore(totalRiskScore)
                .riskLevel(riskLevel)
                .build();
    }

    private int calculateCountryRisk(KycApplication application) {
        // This would normally check country risk ratings from database
        // For demo, using simple logic
        String[] highRiskCountries = {"IR", "SY", "KP", "CU", "VE"};
        String[] mediumRiskCountries = {"RU", "CN", "PK", "AF", "IQ"};

        // For demo, we'll just return a random score based on "country"
        // In real implementation, you'd extract country from document or customer data
        return random.nextInt(30); // 0-29 points
    }

    private int calculateDocumentTypeRisk(KycApplication application) {
        switch (application.getDocumentType().toUpperCase()) {
            case "PASSPORT":
                return 10; // Passports are generally reliable
            case "DRIVING_LICENSE":
                return 20; // Driving licenses can be easier to forge
            case "NATIONAL_ID":
                return 15; // National IDs are moderately reliable
            default:
                return 25; // Unknown document types are higher risk
        }
    }

    private int calculateNameComplexityRisk(KycApplication application) {
        // Check if name has special characters or is very short/long
        String name = application.getCustomerName();
        if (name == null || name.trim().isEmpty()) {
            return 30;
        }

        int risk = 0;
        if (name.length() < 3) risk += 15;
        if (name.matches(".*[^a-zA-Z\\s].*")) risk += 10; // Special characters
        if (name.split(" ").length < 2) risk += 5; // Single name only

        return risk;
    }

    private int calculateRandomRiskFactor() {
        return random.nextInt(10); // 0-9 random points
    }

    private RiskLevel determineRiskLevel(int score) {
        if (score < 30) {
            return RiskLevel.LOW;
        } else if (score < 70) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.HIGH;
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class RiskAssessmentResult {
        private int riskScore;
        private RiskLevel riskLevel;
    }
}
