package com.zyloerp.modules.usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UsuarioDTO {

    private Long codigoUsuario;
    private String nomeCompleto;
    private String email;
    private String perfil;
    private Boolean ativo;
}
