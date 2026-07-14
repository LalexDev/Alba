package com.optica.optistock.controller;

import com.optica.optistock.dto.LoginRequest;
import com.optica.optistock.dto.RegisterAdminRequest;
import com.optica.optistock.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public Object login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/register-admin")
    public Object registerAdmin(@RequestBody @Valid RegisterAdminRequest request) {
        return authService.registerAdmin(request);
    }

    @GetMapping("/me")
    public Object me(Authentication authentication) {
        return authService.me(authentication.getName());
    }
}
