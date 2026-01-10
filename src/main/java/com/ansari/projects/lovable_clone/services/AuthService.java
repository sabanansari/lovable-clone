package com.ansari.projects.lovable_clone.services;

import com.ansari.projects.lovable_clone.dto.auth.AuthResponse;
import com.ansari.projects.lovable_clone.dto.auth.LoginRequest;
import com.ansari.projects.lovable_clone.dto.auth.SignupRequest;
import org.jspecify.annotations.Nullable;

public interface AuthService {
    AuthResponse signup(SignupRequest request);

    AuthResponse login(LoginRequest request);
}
