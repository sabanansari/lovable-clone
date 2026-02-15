package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.project.ProjectRequest;
import com.ansari.projects.lovable_clone.dto.project.ProjectResponse;
import com.ansari.projects.lovable_clone.dto.project.ProjectSummaryResponse;
import com.ansari.projects.lovable_clone.entities.Project;
import com.ansari.projects.lovable_clone.entities.ProjectMember;
import com.ansari.projects.lovable_clone.entities.ProjectMemberId;
import com.ansari.projects.lovable_clone.entities.User;
import com.ansari.projects.lovable_clone.enums.ProjectRole;
import com.ansari.projects.lovable_clone.error.BadRequestException;
import com.ansari.projects.lovable_clone.error.ResourceNotFoundException;
import com.ansari.projects.lovable_clone.mapper.ProjectMapper;
import com.ansari.projects.lovable_clone.repository.ProjectMemberRepository;
import com.ansari.projects.lovable_clone.repository.ProjectRepository;
import com.ansari.projects.lovable_clone.repository.UserRepository;
import com.ansari.projects.lovable_clone.security.AuthUtil;
import com.ansari.projects.lovable_clone.services.ProjectService;
import com.ansari.projects.lovable_clone.services.ProjectTemplateService;
import com.ansari.projects.lovable_clone.services.SubscriptionService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
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
    AuthUtil authUtil;
    SubscriptionService subscriptionService;
    ProjectTemplateService projectTemplateService;

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        if(!subscriptionService.canCreateProject()){
            throw new BadRequestException("Cannot create newd project with current plan, upgrade plan now.");
        }

        Long userId = authUtil.getCurrentUserId();
//        User owner = userRepository.findById(userId).orElseThrow(
//                () -> new ResourceNotFoundException("User", userId.toString())
//        );

        User owner = userRepository.getReferenceById(userId); //It will provide Hibernate proxy, won't hit database

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

        projectTemplateService.initializeProjectFromTemplate(project.getId());

        return projectMapper.toProjectResponse(project);
    }

    @Override

    public List<ProjectSummaryResponse> getUserProjects() {
        Long userId = authUtil.getCurrentUserId();
        var projects = projectRepository.findAllAccessibleByUser(userId);
        return projectMapper.toListOfProjectSummaryResponse(projects);
    }

    @Override
    @PreAuthorize("@security.canViewProject(#id)")
    public ProjectResponse getUserProjectById(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id, userId);
        return projectMapper.toProjectResponse(project);
    }



    @Override
    @PreAuthorize("@security.canEditProject(#id)")
    public ProjectResponse updateProject(Long id, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id, userId);
        project.setName(request.name());
        project = projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#id)")
    public void softDelete(Long id) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessibleProjectById(id, userId);

        project.setDeletedAt(Instant.now());
        projectRepository.save(project);
    }

    ///  INTERNAl FUNCTIONS
    private Project getAccessibleProjectById(Long id,Long userId){
        return projectRepository.findAccessibleProjectById(id, userId)
                .orElseThrow(()-> new ResourceNotFoundException("Project", id.toString()));
    }

}
