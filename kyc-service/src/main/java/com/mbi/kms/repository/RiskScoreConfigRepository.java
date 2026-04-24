package com.mbi.kms.repository;


import com.mbi.kms.entity.RiskScoreConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface RiskScoreConfigRepository extends JpaRepository<RiskScoreConfig, Long> {
    List<RiskScoreConfig> findByCriteriaName(String criteriaName);
}
