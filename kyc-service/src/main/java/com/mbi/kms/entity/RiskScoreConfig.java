package com.mbi.kms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "risk_score_config")
@Data
@NoArgsConstructor
public class RiskScoreConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String criteriaName;

    private String criteriaValue;

    private Integer scorePoints;

    private String description;
}
