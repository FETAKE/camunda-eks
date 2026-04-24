package com.mbi.kms.service;


import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.entity.PepList;
import com.mbi.kms.entity.SanctionsList;
import com.mbi.kms.repository.PepListRepository;
import com.mbi.kms.repository.SanctionsListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScreeningService {

    private final SanctionsListRepository sanctionsListRepository;
    private final PepListRepository pepListRepository;

    @Transactional
    public ScreeningResult performScreening(KycApplication application) {
        log.info("Performing sanctions and PEP screening for customer: {}", application.getCustomerName());

        // Check sanctions list
        boolean sanctionsPassed = checkSanctionsList(application);
        application.setSanctionsCheckPassed(sanctionsPassed);

        // Check PEP list
        boolean pepPassed = checkPepList(application);
        application.setPepCheckPassed(pepPassed);

        return ScreeningResult.builder()
                .sanctionsPassed(sanctionsPassed)
                .pepPassed(pepPassed)
                .build();
    }

    private boolean checkSanctionsList(KycApplication application) {
        // Check by name
        List<SanctionsList> sanctionsByName = sanctionsListRepository
                .findByNameOrAlias(application.getCustomerName());

        if (!sanctionsByName.isEmpty()) {
            log.warn("Customer found in sanctions list by name: {}", application.getCustomerName());
            return false;
        }

        // Check by document number if available
        if (application.getDocumentNumber() != null) {
            List<SanctionsList> sanctionsByDoc = sanctionsListRepository
                    .findByPassportNumber(application.getDocumentNumber());

            if (!sanctionsByDoc.isEmpty()) {
                log.warn("Document number found in sanctions list: {}", application.getDocumentNumber());
                return false;
            }
        }

        return true;
    }

    private boolean checkPepList(KycApplication application) {
        List<PepList> pepMatches = pepListRepository.findByName(application.getCustomerName());

        if (!pepMatches.isEmpty()) {
            log.info("Customer found in PEP list: {}", application.getCustomerName());
            return false; // PEP match found
        }

        return true; // No PEP match
    }

    @lombok.Builder
    @lombok.Data
    public static class ScreeningResult {
        private boolean sanctionsPassed;
        private boolean pepPassed;
    }
}
