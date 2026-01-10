package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.auth.UserProfileResponse;

public interface UserService {

    UserProfileResponse getProfile(Long userId);
}
