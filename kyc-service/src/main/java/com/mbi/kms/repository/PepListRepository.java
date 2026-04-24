package com.mbi.kms.repository;


import com.mbi.kms.entity.PepList;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface PepListRepository extends JpaRepository<PepList, Long> {

    @Query("SELECT p FROM PepList p WHERE " +
            "LOWER(p.fullName) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<PepList> findByName(@Param("name") String name);

    List<PepList> findByCountryAndActiveTrue(String country);
}
