package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Query("""
    SELECT p FROM Project p
    LEFT JOIN FETCH p.owner
    WHERE p.id = :projectId
    and p.deletedAt IS NULL
    and p.owner.id = :userId
    """
    )
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);
}
