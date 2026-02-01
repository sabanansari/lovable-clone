package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.Plan;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
}
