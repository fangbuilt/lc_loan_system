package com.fangbuilt.lc_loan_system.shared.service;

import com.fangbuilt.lc_loan_system.shared.domain.BaseEntity;
import com.fangbuilt.lc_loan_system.shared.domain.PageRequest;
import com.fangbuilt.lc_loan_system.shared.exception.ResourceNotFoundException;
import com.fangbuilt.lc_loan_system.shared.repository.BaseRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public abstract class BaseCrudService<T extends BaseEntity, R extends BaseRepository<T>> {
    
    protected abstract R getRepository();
    protected abstract String getEntityName();
    
    /**
     * CREATE - Single entity
     */
    @Transactional
    public T create(T entity) {
        log.debug("Creating {} entity", getEntityName());
        return getRepository().save(entity);
    }
    
    /**
     * CREATE - Bulk
     */
    @Transactional
    public List<T> bulkCreate(List<T> entities) {
        log.debug("Bulk creating {} {} entities", entities.size(), getEntityName());
        return getRepository().saveAll(entities);
    }
    
    /**
     * READ - By ID
     */
    public T findById(UUID id) {
        return getRepository().findByIdActive(id)
            .orElseThrow(() -> new ResourceNotFoundException(
                getEntityName() + " not found with id: " + id
            ));
    }
    
    /**
     * READ - All with pagination, filtering, sorting
     */
    public Page<T> findAll(PageRequest pageRequest) {
        log.debug("Finding all {} with filters: {}", getEntityName(), pageRequest.getFilters());
        
        Specification<T> spec = buildSpecification(pageRequest);
        Pageable pageable = pageRequest.toPageable();
        
        return getRepository().findAll(spec, pageable);
    }
    
    /**
     * UPDATE - Single entity
     */
    @Transactional
    public T update(UUID id, T updatedEntity) {
        log.debug("Updating {} with id: {}", getEntityName(), id);
        
        T existing = findById(id);
        updateFields(existing, updatedEntity);
        
        return getRepository().save(existing);
    }
    
    /**
     * UPDATE - Bulk
     */
    @Transactional
    public List<T> bulkUpdate(List<UUID> ids, T template) {
        log.debug("Bulk updating {} {} entities", ids.size(), getEntityName());
        
        List<T> entities = getRepository().findAllById(ids);
        entities.forEach(entity -> {
            if (!entity.isDeleted()) {
                updateFields(entity, template);
            }
        });
        
        return getRepository().saveAll(entities);
    }
    
    /**
     * DELETE (SOFT) - Single
     */
    @Transactional
    public void delete(UUID id) {
        log.debug("Soft deleting {} with id: {}", getEntityName(), id);
        
        // Verify exists
        findById(id);
        
        getRepository().softDelete(id, LocalDateTime.now());
    }
    
    /**
     * DELETE (SOFT) - Bulk
     */
    @Transactional
    public void bulkDelete(List<UUID> ids) {
        log.debug("Bulk soft deleting {} {} entities", ids.size(), getEntityName());
        getRepository().softDeleteBatch(ids, LocalDateTime.now());
    }
    
    /**
     * DELETE (SOFT) - All
     */
    @Transactional
    public void deleteAll() {
        log.warn("Soft deleting ALL {} entities", getEntityName());
        getRepository().softDeleteAll(LocalDateTime.now());
    }
    
    /**
     * Build dynamic specification for filtering
     */
    private Specification<T> buildSpecification(PageRequest pageRequest) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            
            // Always exclude soft-deleted
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            // Apply filters
            pageRequest.getFilters().forEach((key, value) -> {
                if (value != null && !value.toString().isEmpty()) {
                    if (value instanceof String strValue && strValue.contains("%")) {
                        // LIKE query
                        predicates.add(
                            criteriaBuilder.like(
                                criteriaBuilder.lower(root.get(key).as(String.class)),
                                strValue.toLowerCase()
                            )
                        );
                    } else if (value instanceof List<?> listValue) {
                        // IN query
                        predicates.add(root.get(key).in(listValue));
                    } else {
                        // Exact match
                        predicates.add(criteriaBuilder.equal(root.get(key), value));
                    }
                }
            });
            
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }
    
    /**
     * Override this method to handle field updates
     */
    protected abstract void updateFields(T existing, T updated);
}
