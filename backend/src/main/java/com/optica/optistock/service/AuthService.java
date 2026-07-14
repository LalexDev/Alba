package com.optica.optistock.service;

import com.optica.optistock.dto.*;

public interface AuthService {
    LoginResponse login(LoginRequest request);
    UsuarioDto registerAdmin(RegisterAdminRequest request);
    UsuarioDto me(String username);
}
