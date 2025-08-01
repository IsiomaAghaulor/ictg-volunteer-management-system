package com.lfc.volunteermgtsystem.repositories;

import com.lfc.volunteermgtsystem.entities.Ministry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MinistryRepository extends JpaRepository<Ministry, Long> {
    
    @Query("SELECT m FROM Ministry m WHERE m.active = true")
    Page<Ministry> findAllActive(Pageable pageable);
    
    @Query("SELECT m FROM Ministry m WHERE m.active = true")
    List<Ministry> findAllActive();
    
    @Query("SELECT m FROM Ministry m WHERE m.parentMinistry IS NULL AND m.active = true")
    List<Ministry> findRootMinistries();
    
    @Query("SELECT m FROM Ministry m WHERE m.parentMinistry.id = :parentId AND m.active = true")
    List<Ministry> findByParentMinistryId(@Param("parentId") Long parentId);
    
    @Query("SELECT m FROM Ministry m WHERE " +
           "LOWER(m.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) AND " +
           "m.active = true")
    Page<Ministry> searchActiveMinistries(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    Boolean existsByNameAndActiveTrue(String name);
}