package com.ansari.projects.lovable_clone.mapper;

import com.ansari.projects.lovable_clone.dto.member.MemberResponse;
import com.ansari.projects.lovable_clone.entities.ProjectMember;
import com.ansari.projects.lovable_clone.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

    @Mapping(target="userId",source="id")
    @Mapping(target="role",constant="OWNER")
    MemberResponse toProjectMemberResponseFromOwner(User owner);

    @Mapping(target="userId",source="user.id")
    @Mapping(target="username",source="user.username")
    @Mapping(target="name",source="user.name")
    @Mapping(target="role",source="projectRole")
    @Mapping(target="avatarUrl",source="user.avatarUrl")
    MemberResponse toProjectMemberResponseFromMember(ProjectMember projectMember);

}
