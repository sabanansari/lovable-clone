package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.project.ProjectRequest;
import com.ansari.projects.lovable_clone.dto.project.ProjectResponse;
import com.ansari.projects.lovable_clone.dto.project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {
    List<ProjectSummaryResponse> getUserProjects();

    ProjectResponse getUserProjectById(Long id);

    ProjectResponse createProject(ProjectRequest request);

    ProjectResponse updateProject(Long id, ProjectRequest request);

    void softDelete(Long id);
}
