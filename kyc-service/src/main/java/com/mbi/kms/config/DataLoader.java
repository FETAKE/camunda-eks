package com.mbi.kms.config;

import com.mbi.kms.entity.PepList;
import com.mbi.kms.entity.RiskScoreConfig;
import com.mbi.kms.entity.SanctionsList;
import com.mbi.kms.entity.ValidDocument;
import com.mbi.kms.repository.PepListRepository;
import com.mbi.kms.repository.RiskScoreConfigRepository;
import com.mbi.kms.repository.SanctionsListRepository;
import com.mbi.kms.repository.ValidDocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements CommandLineRunner {

    private final SanctionsListRepository sanctionsListRepository;
    private final PepListRepository pepListRepository;
    private final ValidDocumentRepository validDocumentRepository;
    private final RiskScoreConfigRepository riskScoreConfigRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Loading sample data into H2 database...");

        // Load sanctions list
        loadSanctionsList();

        // Load PEP list
        loadPepList();

        // Load valid documents
        loadValidDocuments();

        // Load risk score configurations
        loadRiskScoreConfigs();

        log.info("Sample data loaded successfully!");
    }

    private void loadSanctionsList() {
        if (sanctionsListRepository.count() == 0) {
            SanctionsList s1 = new SanctionsList();
            s1.setFullName("John Smith");
            s1.setAlias("Johnny Smith");
            s1.setCountry("US");
            s1.setPassportNumber("AB123456");
            s1.setReason("Financial crimes");
            s1.setListedDate("2020-01-15");
            sanctionsListRepository.save(s1);

            SanctionsList s2 = new SanctionsList();
            s2.setFullName("Vladimir Petrov");
            s2.setAlias("Vlad Petrov");
            s2.setCountry("RU");
            s2.setPassportNumber("CD789012");
            s2.setReason("Money laundering");
            s2.setListedDate("2019-06-20");
            sanctionsListRepository.save(s2);

            SanctionsList s3 = new SanctionsList();
            s3.setFullName("Chen Wei");
            s3.setCountry("CN");
            s3.setPassportNumber("EF345678");
            s3.setReason("Export violations");
            s3.setListedDate("2021-03-10");
            sanctionsListRepository.save(s3);

            log.info("Loaded {} sanctions list entries", sanctionsListRepository.count());
        }
    }

    private void loadPepList() {
        if (pepListRepository.count() == 0) {
            PepList p1 = new PepList();
            p1.setFullName("Angela Merkel");
            p1.setPoliticalPosition("Former Chancellor");
            p1.setCountry("DE");
            p1.setStartDate("2005-11-22");
            p1.setEndDate("2021-12-08");
            pepListRepository.save(p1);

            PepList p2 = new PepList();
            p2.setFullName("Narendra Modi");
            p2.setPoliticalPosition("Prime Minister");
            p2.setCountry("IN");
            p2.setStartDate("2014-05-26");
            pepListRepository.save(p2);

            PepList p3 = new PepList();
            p3.setFullName("Justin Trudeau");
            p3.setPoliticalPosition("Prime Minister");
            p3.setCountry("CA");
            p3.setStartDate("2015-11-04");
            pepListRepository.save(p3);

            log.info("Loaded {} PEP list entries", pepListRepository.count());
        }
    }

    private void loadValidDocuments() {
        if (validDocumentRepository.count() == 0) {
            ValidDocument d1 = new ValidDocument();
            d1.setDocumentType("PASSPORT");
            d1.setDocumentNumber("XH1234567");
            d1.setIssuedBy("India");
            d1.setIssueDate("2020-05-15");
            d1.setExpiryDate("2030-05-14");
            d1.setCustomerName("Rajesh Kumar");
            validDocumentRepository.save(d1);

            ValidDocument d2 = new ValidDocument();
            d2.setDocumentType("DRIVING_LICENSE");
            d2.setDocumentNumber("DL9876543");
            d2.setIssuedBy("California DMV");
            d2.setIssueDate("2019-08-22");
            d2.setExpiryDate("2025-08-21");
            d2.setCustomerName("John Davis");
            validDocumentRepository.save(d2);

            ValidDocument d3 = new ValidDocument();
            d3.setDocumentType("NATIONAL_ID");
            d3.setDocumentNumber("ID4567890");
            d3.setIssuedBy("UK");
            d3.setIssueDate("2018-11-30");
            d3.setExpiryDate("2028-11-29");
            d3.setCustomerName("Emma Watson");
            validDocumentRepository.save(d3);

            log.info("Loaded {} valid documents", validDocumentRepository.count());
        }
    }

    private void loadRiskScoreConfigs() {
        if (riskScoreConfigRepository.count() == 0) {
            RiskScoreConfig c1 = new RiskScoreConfig();
            c1.setCriteriaName("COUNTRY_RISK_HIGH");
            c1.setCriteriaValue("IR,SY,KP,CU,VE");
            c1.setScorePoints(50);
            c1.setDescription("High risk countries");
            riskScoreConfigRepository.save(c1);

            RiskScoreConfig c2 = new RiskScoreConfig();
            c2.setCriteriaName("COUNTRY_RISK_MEDIUM");
            c2.setCriteriaValue("RU,CN,PK,AF,IQ");
            c2.setScorePoints(25);
            c2.setDescription("Medium risk countries");
            riskScoreConfigRepository.save(c2);

            RiskScoreConfig c3 = new RiskScoreConfig();
            c3.setCriteriaName("DOCUMENT_PASSPORT");
            c3.setScorePoints(10);
            c3.setDescription("Passport document risk score");
            riskScoreConfigRepository.save(c3);

            RiskScoreConfig c4 = new RiskScoreConfig();
            c4.setCriteriaName("DOCUMENT_DRIVING_LICENSE");
            c4.setScorePoints(20);
            c4.setDescription("Driving license risk score");
            riskScoreConfigRepository.save(c4);

            log.info("Loaded {} risk score configurations", riskScoreConfigRepository.count());
        }
    }
}
