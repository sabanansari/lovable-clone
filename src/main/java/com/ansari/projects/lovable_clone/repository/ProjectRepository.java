package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {

    @Query("""
            select p FROM Project p
            where p.deletedAt IS NULL
            and (p.owner.id = :userId)
            order by p.updatedAt DESC        
            """
    )
    List<Project> findAllAccessibleByUser(@Param("userId") Long userId);
}
