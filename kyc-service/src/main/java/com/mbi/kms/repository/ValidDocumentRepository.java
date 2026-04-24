package com.mbi.kms.repository;


import com.mbi.kms.entity.ValidDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface ValidDocumentRepository extends JpaRepository<ValidDocument, Long> {

    Optional<ValidDocument> findByDocumentNumber(String documentNumber);

    @Query("SELECT v FROM ValidDocument v WHERE " +
            "v.documentNumber = :documentNumber AND " +
            "v.documentType = :documentType AND " +
            "v.customerName = :customerName")
    Optional<ValidDocument> verifyDocument(
            @Param("documentNumber") String documentNumber,
            @Param("documentType") String documentType,
            @Param("customerName") String customerName);
}
