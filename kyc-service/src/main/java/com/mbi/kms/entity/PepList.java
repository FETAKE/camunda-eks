package com.mbi.kms.entity;


import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pep_list")
@Data
@NoArgsConstructor
public class PepList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;

    private String politicalPosition;

    @Column(nullable = false)
    private String country;

    private String startDate;

    private String endDate;

    private Boolean active = true;
}