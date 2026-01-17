package com.zyloerp.modules.auth.dto;

import com.zyloerp.modules.usuario.dto.UsuarioDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tipo;
    private Long expiresIn;
    private UsuarioDTO usuario;
}