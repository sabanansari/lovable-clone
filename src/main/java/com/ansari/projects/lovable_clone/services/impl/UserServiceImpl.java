package com.ansari.projects.lovable_clone.services.impl;

import com.ansari.projects.lovable_clone.dto.auth.UserProfileResponse;
import com.ansari.projects.lovable_clone.services.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Override
    public UserProfileResponse getProfile(Long userId) {
        return null;
    }
}
