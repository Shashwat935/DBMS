package com.walmart.truckmgt.repository;

import com.walmart.truckmgt.model.entity.CompatibilityRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for CompatibilityRule (load type transition cleaning rules).
 */
@Repository
public interface CompatibilityRuleRepository extends JpaRepository<CompatibilityRule, Long> {

    /**
     * Look up the cleaning rule for a specific load type transition.
     */
    @Query("SELECT cr FROM CompatibilityRule cr " +
           "WHERE cr.fromLoadType.id = :fromId AND cr.toLoadType.id = :toId")
    Optional<CompatibilityRule> findByFromAndTo(
            @Param("fromId") Long fromLoadTypeId,
            @Param("toId") Long toLoadTypeId);
}
