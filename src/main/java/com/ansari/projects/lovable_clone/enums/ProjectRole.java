package com.ansari.projects.lovable_clone.enums;

import lombok.Getter;

import java.util.Set;

import static com.ansari.projects.lovable_clone.enums.ProjectPermission.*;

@Getter
public enum ProjectRole {

    OWNER(EDIT,VIEW,DELETE,VIEW_MEMBERS,MANAGE_MEMBERS),
    EDITOR(VIEW,EDIT,DELETE,VIEW_MEMBERS),
    VIEWER(VIEW,VIEW_MEMBERS);

    ProjectRole(ProjectPermission... permissions) {
        this.permissions = Set.of(permissions);
    }

    private final Set<ProjectPermission> permissions;
}
