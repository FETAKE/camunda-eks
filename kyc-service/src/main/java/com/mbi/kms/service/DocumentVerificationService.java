package com.mbi.kms.service;


import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.entity.ValidDocument;
import com.mbi.kms.repository.ValidDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentVerificationService {

    private final ValidDocumentRepository validDocumentRepository;

    @Transactional
    public DocumentVerificationResult verifyDocument(KycApplication application) {
        log.info("Verifying document for customer: {}", application.getCustomerName());

        // Check if document exists in our valid documents database
        Optional<ValidDocument> validDocumentOpt = validDocumentRepository
                .verifyDocument(
                        application.getDocumentNumber(),
                        application.getDocumentType(),
                        application.getCustomerName()
                );

        boolean documentValid = false;
        String verificationMessage = "";

        if (validDocumentOpt.isPresent()) {
            ValidDocument validDocument = validDocumentOpt.get();

            // Check if document is expired
            if (isDocumentExpired(validDocument.getExpiryDate())) {
                verificationMessage = "Document has expired";
                log.warn("Document expired: {}", application.getDocumentNumber());
            } else {
                documentValid = true;
                verificationMessage = "Document verified successfully";
                log.info("Document verified successfully for: {}", application.getCustomerName());
            }
        } else {
            verificationMessage = "Document not found in valid documents database";
            log.warn("Document not found: {}", application.getDocumentNumber());
        }

        application.setDocumentVerificationPassed(documentValid);

        return DocumentVerificationResult.builder()
                .verified(documentValid)
                .message(verificationMessage)
                .build();
    }

    private boolean isDocumentExpired(String expiryDateStr) {
        if (expiryDateStr == null || expiryDateStr.isEmpty()) {
            return false; // No expiry date means never expires
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate expiryDate = LocalDate.parse(expiryDateStr, formatter);
            return expiryDate.isBefore(LocalDate.now());
        } catch (Exception e) {
            log.error("Error parsing expiry date: {}", expiryDateStr, e);
            return true; // If can't parse, consider it invalid
        }
    }

    @lombok.Builder
    @lombok.Data
    public static class DocumentVerificationResult {
        private boolean verified;
        private String message;
    }
}