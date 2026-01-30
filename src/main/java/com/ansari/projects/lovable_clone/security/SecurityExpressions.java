package com.ansari.projects.lovable_clone.security;

import com.ansari.projects.lovable_clone.enums.ProjectPermission;
import com.ansari.projects.lovable_clone.repository.ProjectMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import static com.ansari.projects.lovable_clone.enums.ProjectPermission.*;

@Component("security")
@RequiredArgsConstructor
public class SecurityExpressions {

    private final ProjectMemberRepository projectMemberRepository;
    private final AuthUtil authUtil;

    private boolean hasPermission(Long projectId, ProjectPermission permission) {
        Long userId = authUtil.getCurrentUserId();
        return projectMemberRepository.findRoleByProjectIdAndUserId(projectId, userId)
                .map(role -> role.getPermissions().contains(permission))
                .orElse(false);
    }

    public boolean canViewProject(Long projectId) {
        return hasPermission(projectId, VIEW);

    }

    public boolean canEditProject(Long projectId) {
        return hasPermission(projectId, EDIT);
    }

    public boolean canDeleteProject(Long projectId) {
        return hasPermission(projectId, DELETE);
    }

    public boolean canViewMembers(Long projectId) {
        return hasPermission(projectId, VIEW_MEMBERS);
    }

    public boolean canManageMembers(Long projectId) {
        return hasPermission(projectId, MANAGE_MEMBERS);
    }
}

