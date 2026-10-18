package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.entity.LoadType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for LoadType master data.
 */
@Repository
public interface LoadTypeRepository extends JpaRepository<LoadType, Long> {

    /** Find a load type by its unique name (e.g. "COAL") */
    Optional<LoadType> findByName(String name);
}
