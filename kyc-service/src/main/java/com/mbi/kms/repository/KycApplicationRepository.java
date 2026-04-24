package com.mbi.kms.repository;

import com.mbi.kms.entity.KycApplication;
import com.mbi.kms.entity.ApplicationStatus;
import com.mbi.kms.entity.RiskLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface KycApplicationRepository extends JpaRepository<KycApplication, Long> {
    List<KycApplication> findByStatus(ApplicationStatus status);
    KycApplication findByProcessInstanceId(String processInstanceId);
    List<KycApplication> findByAssignedAnalyst(String analyst);
    Optional<KycApplication> findByCustomerEmail(String customerEmail);
    List<KycApplication> findByRiskLevel(RiskLevel riskLevel);
}
