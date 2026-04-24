package com.mbi.kms.repository;


import com.mbi.kms.entity.SanctionsList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SanctionsListRepository extends JpaRepository<SanctionsList, Long> {

    @Query("SELECT s FROM SanctionsList s WHERE " +
            "LOWER(s.fullName) LIKE LOWER(CONCAT('%', :name, '%')) OR " +
            "LOWER(s.alias) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<SanctionsList> findByNameOrAlias(@Param("name") String name);

    @Query("SELECT s FROM SanctionsList s WHERE " +
            "s.passportNumber = :passportNumber")
    List<SanctionsList> findByPassportNumber(@Param("passportNumber") String passportNumber);

    List<SanctionsList> findByCountryAndActiveTrue(String country);
}
