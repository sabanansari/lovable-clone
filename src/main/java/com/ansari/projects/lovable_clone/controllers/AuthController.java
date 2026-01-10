package com.ansari.projects.lovable_clone.controllers;

import com.ansari.projects.lovable_clone.dto.auth.AuthResponse;
import com.ansari.projects.lovable_clone.dto.auth.LoginRequest;
import com.ansari.projects.lovable_clone.dto.auth.SignupRequest;
import com.ansari.projects.lovable_clone.dto.auth.UserProfileResponse;
import com.ansari.projects.lovable_clone.services.AuthService;
import com.ansari.projects.lovable_clone.services.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.coyote.Response;
import org.springframework.http.RequestEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(SignupRequest request){
        return ResponseEntity.ok(authService.signup(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(LoginRequest request){
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(){
        Long userId = 1L;
        return ResponseEntity.ok(userService.getProfile(userId));
    }
}
