package com.fangbuilt.lc_loan_system.shared.repository;

import com.fangbuilt.lc_loan_system.shared.domain.BaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@NoRepositoryBean
public interface BaseRepository<T extends BaseEntity> 
    extends JpaRepository<T, UUID>, JpaSpecificationExecutor<T> {
    
    /**
     * Find all entities excluding soft-deleted ones
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.deletedAt IS NULL")
    List<T> findAllActive();
    
    /**
     * Find by ID excluding soft-deleted
     */
    @Query("SELECT e FROM #{#entityName} e WHERE e.id = :id AND e.deletedAt IS NULL")
    Optional<T> findByIdActive(@Param("id") UUID id);
    
    /**
     * Soft delete by ID
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deletedAt = :now WHERE e.id = :id")
    void softDelete(@Param("id") UUID id, @Param("now") LocalDateTime now);
    
    /**
     * Bulk soft delete
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deletedAt = :now WHERE e.id IN :ids")
    void softDeleteBatch(@Param("ids") List<UUID> ids, @Param("now") LocalDateTime now);
    
    /**
     * Soft delete all
     */
    @Modifying
    @Query("UPDATE #{#entityName} e SET e.deletedAt = :now")
    void softDeleteAll(@Param("now") LocalDateTime now);
}
