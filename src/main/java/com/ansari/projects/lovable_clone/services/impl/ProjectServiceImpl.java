package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.project.ProjectRequest;
import com.ansari.projects.lovable_clone.dto.project.ProjectResponse;
import com.ansari.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.ansari.projects.lovable_clone.entities.Project;
import com.ansari.projects.lovable_clone.entities.ProjectMember;
import com.ansari.projects.lovable_clone.entities.ProjectMemberId;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.enums.ProjectRole;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.mapper.ProjectMapper;
import com.ansari.projects.lovable_clone.repository.ProjectMemberRepository;
import com.ansari.projects.lovable_clone.repository.ProjectRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.services.ProjectService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Transactional
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    UserRepository userRepository;
    ProjectMapper projectMapper;

    @Override
    public ProjectResponse createProject(ProjectRequest request, Long userId) {
        User owner = userRepository.findById(userId).orElseThrow(
                () -> new ResourceNotFoundException("User", userId.toString())
        );
        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();

        project = projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(), owner.getId());

        ProjectMember member = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .user(owner)
                .acceptedAt(Instant.now())
                .invitedAt(Instant.now())
                .project(project)
                .build();

        projectMemberRepository.save(member);


        return projectMapper.toProjectResponse(project);
    }

    @Override
    public List<ProjectSummaryResponse> getUserProjects(Long userId) {
        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toListOfProjectSummaryResponse(projects);
    }

    @Override
    public ProjectResponse getUserProjectById(Long id, Long userId) {
        Project project = getAccessibleProjectById(id, userId);
        return projectMapper.toProjectResponse(project);
    }



    @Override
    public ProjectResponse updateProject(Long id, ProjectRequest request, Long userId) {
        Project project = getAccessibleProjectById(id, userId);
        project.setName(request.name());
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    public void softDelete(Long id, Long userId) {
        Project project = getAccessibleProjectById(id, userId);

        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

    ///  INTERNAl FUNCTIONS
    private Project getAccessibleProjectById(Long id, Long userId){
        return projectRepository.findAccessibleProjectById(id, userId)
                .orElseThrow(()-> new ResourceNotFoundException("Project", id.toString()));
    }

}
