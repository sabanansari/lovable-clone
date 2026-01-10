package com.ansari.projects.lovable_clone.dto.member;

import com.ansari.projects.lovable_clone.enums.ProjectRole;

public record UpdateMemberRoleRequest(
        ProjectRole role
) {
}
