package com.dtacomp.dto.auth;

import java.util.UUID;

public record AuthResponse(
    String token,
    String email,
    String perfil,
    UUID id
) {}
