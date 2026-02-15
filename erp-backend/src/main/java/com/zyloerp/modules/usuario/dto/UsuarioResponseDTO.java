package com.zyloerp.modules.usuario.dto;

import com.zyloerp.modules.usuario.model.Usuario;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {

    private Long codigoUsuario;
    private String nomeCompleto;
    private String email;
    private String perfil;
    private Long codigoPerfil;
    private Boolean ativo;
    private LocalDateTime ultimoAcesso;
    private LocalDateTime criadoEm;

    public static UsuarioResponseDTO fromEntity(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .codigoUsuario(usuario.getCodigoUsuario())
                .nomeCompleto(usuario.getNomeCompleto())
                .email(usuario.getEmail())
                .perfil(usuario.getNomePerfil())
                .codigoPerfil(usuario.getPerfil() != null ? usuario.getPerfil().getCodigoPerfil() : null)
                .ativo(usuario.getAtivo())
                .ultimoAcesso(usuario.getUltimoAcesso())
                .criadoEm(usuario.getCriadoEm())
                .build();
    }
}