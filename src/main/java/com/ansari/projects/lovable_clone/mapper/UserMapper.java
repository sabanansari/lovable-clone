package com.ansari.projects.lovable_clone.mapper;

import com.ansari.projects.lovable_clone.dto.auth.SignupRequest;
import com.ansari.projects.lovable_clone.dto.auth.UserProfileResponse;
import com.ansari.projects.lovable_clone.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    User toEntity(SignupRequest request);

    UserProfileResponse toUserProfileResponse(User user);
}
