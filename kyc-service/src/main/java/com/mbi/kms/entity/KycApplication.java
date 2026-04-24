package com.mbi.kms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "kyc_applications")
@Data
@NoArgsConstructor
public class KycApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false, unique = true)
    private String customerEmail;

    @Column(nullable = false)
    private String customerPhone;

    @Column(nullable = false)
    private String documentType; // PASSPORT, DRIVING_LICENSE, NATIONAL_ID

    @Column(nullable = false)
    private String documentNumber;

    @Column(length = 5000)
    private String documentImageUrl; // URL to stored document image

    private String country;

    private String dateOfBirth;

    @Column(nullable = false)
    private LocalDateTime submissionDate;

    private LocalDateTime lastUpdatedDate;

    @Enumerated(EnumType.STRING)
    private ApplicationStatus status;

    @Enumerated(EnumType.STRING)
    private RiskLevel riskLevel;

    private Integer riskScore;

    private Boolean sanctionsCheckPassed;

    private Boolean pepCheckPassed;

    private Boolean documentVerificationPassed;

    private String rejectionReason;

    private String assignedAnalyst;

    private String processInstanceId; // Camunda process instance ID

    private String comments; // Additional comments from analysts
}
