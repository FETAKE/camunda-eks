package com.mbi.kms.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "sanctions_list")
@Data
@NoArgsConstructor
public class SanctionsList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String alias;

    @Column(nullable = false)
    private String country;

    private String passportNumber;

    private String reason;

    @Column(name = "listed_date")
    private String listedDate;

    private Boolean active = true;
}
