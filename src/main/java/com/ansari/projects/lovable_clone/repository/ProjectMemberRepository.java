package com.ansari.projects.lovable_clone.repository;

import com.ansari.projects.lovable_clone.entities.ProjectMember;
import com.ansari.projects.lovable_clone.entities.ProjectMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    List<ProjectMember> findByIdProjectId(Long projectId);
}
