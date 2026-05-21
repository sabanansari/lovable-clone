package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.Project;
import com.ansari.projects.lovable_clone.enums.ProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProjectRepository extends JpaRepository<Project,Long> {

    @Query("""
            select p as project, pm.projectRole as role FROM Project p
                        JOIN ProjectMember pm ON pm.project.id = p.id
            where pm.user.id = :userId 
                        and p.deletedAt IS NULL
            order by p.updatedAt DESC
            """
    )
    List<ProjectWithRole> findAllAccessibleByUser(@Param("userId") Long userId);

    @Query("""
    SELECT p FROM Project p
    WHERE p.id = :projectId
    and p.deletedAt IS NULL
        and exists(
                       select 1 FROM ProjectMember pm
                                   where pm.id.userId = :userId
                                               and pm.id.projectId = :projectId
                        )
    """
    )
    Optional<Project> findAccessibleProjectById(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);

    @Query("""
    SELECT p as project,pm.projectRole as role FROM Project p
        JOIN ProjectMember pm ON pm.project.id = p.id
    WHERE p.id = :projectId AND pm.user.id = :userId
    and p.deletedAt IS NULL
    """
    )
    Optional<ProjectWithRole> findAccessibleProjectByIdWithRole(@Param("projectId") Long projectId,
                                                @Param("userId") Long userId);

    interface ProjectWithRole{
        Project getProject();
        ProjectRole getRole();
    }
}
