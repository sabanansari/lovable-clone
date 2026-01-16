package com.ansari.projects.lovable_clone.mapper;

import com.ansari.projects.lovable_clone.dto.project.ProjectResponse;
import com.ansari.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.ansari.projects.lovable_clone.entities.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    @Mapping(source="name", target="projectName")
    ProjectSummaryResponse toProjectSummaryResponse(Project project);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);

}
