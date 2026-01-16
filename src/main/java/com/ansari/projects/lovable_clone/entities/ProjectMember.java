package com.ansari.projects.lovable_clone.entities;

import com.ansari.projects.lovable_clone.enums.ProjectRole;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProjectMember {

    ProjectMemberId id;

    Project project;

    User user;

    ProjectRole projectRole;

    Instant invitedAt;
    Instant acceptedAt;
}
