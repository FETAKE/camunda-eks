package com.mbi.kms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "valid_documents")
@Data
@NoArgsConstructor
public class ValidDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentType;

    @Column(nullable = false, unique = true)
    private String documentNumber;

    private String issuedBy;

    private String issueDate;

    private String expiryDate;

    @Column(nullable = false)
    private String customerName;

    private Boolean isValid = true;
}