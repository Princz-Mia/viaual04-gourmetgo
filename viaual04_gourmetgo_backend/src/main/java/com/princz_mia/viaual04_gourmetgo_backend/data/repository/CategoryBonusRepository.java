package com.princz_mia.viaual04_gourmetgo_backend.data.repository;

import com.princz_mia.viaual04_gourmetgo_backend.data.entity.CategoryBonus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryBonusRepository extends JpaRepository<CategoryBonus, Long> {
    
    @Query("SELECT cb FROM CategoryBonus cb WHERE cb.categoryName = ?1 AND cb.active = true AND ?2 BETWEEN cb.startTime AND cb.endTime")
    Optional<CategoryBonus> findActiveBonusForCategory(String categoryName, LocalDateTime now);
    
    @Query("SELECT cb FROM CategoryBonus cb WHERE cb.active = true AND ?1 BETWEEN cb.startTime AND cb.endTime")
    List<CategoryBonus> findAllActiveBonuses(LocalDateTime now);
}