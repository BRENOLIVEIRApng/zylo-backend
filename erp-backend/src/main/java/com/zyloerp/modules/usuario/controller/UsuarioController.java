package com.zyloerp.modules.usuario.controller;

import com.zyloerp.modules.usuario.dto.*;
import com.zyloerp.modules.usuario.model.Usuario;
import com.zyloerp.modules.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS:CRIAR')")
    public ResponseEntity<UsuarioResponseDTO> criar(@Valid @RequestBody UsuarioRequestDTO dto) {
        Usuario usuario = usuarioService.criarUsuario(
                dto.getNomeCompleto(),
                dto.getEmail(),
                dto.getSenha(),
                dto.getCodigoPerfil()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(UsuarioResponseDTO.fromEntity(usuario));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarTodos() {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarAtivos()
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<UsuarioResponseDTO> buscarPorId(@PathVariable Long id) {
        Usuario usuario = usuarioService.buscarPorId(id);
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @GetMapping("/perfil/{codigoPerfil}")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorPerfil(@PathVariable Long codigoPerfil) {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarPorPerfil(codigoPerfil)
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/status/{ativo}")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<UsuarioResponseDTO>> listarPorStatus(@PathVariable Boolean ativo) {
        List<UsuarioResponseDTO> usuarios = usuarioService.listarPorStatus(ativo)
                .stream()
                .map(UsuarioResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usuarios);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<UsuarioResponseDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody UsuarioUpdateDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        Usuario usuario = usuarioService.editarUsuario(
                id,
                dto.getNomeCompleto(),
                dto.getEmail(),
                dto.getCodigoPerfil(),
                usuarioLogado.getCodigoUsuario()
        );
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }

    @PatchMapping("/{id}/desativar")
    @PreAuthorize("hasAuthority('USUARIOS:EXCLUIR')")
    public ResponseEntity<Void> desativar(
            @PathVariable Long id,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        usuarioService.desativarUsuario(id, usuarioLogado.getCodigoUsuario());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/ativar")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<Void> ativar(@PathVariable Long id) {
        usuarioService.ativarUsuario(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/resetar-senha")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<Void> resetarSenha(
            @PathVariable Long id,
            @Valid @RequestBody ResetarSenhaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        usuarioService.resetarSenha(id, dto.getNovaSenha(), usuarioLogado.getCodigoUsuario());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/alterar-senha")
    public ResponseEntity<Void> alterarSenha(
            @Valid @RequestBody AlterarSenhaDTO dto,
            @AuthenticationPrincipal Usuario usuarioLogado
    ) {
        usuarioService.alterarSenha(
                usuarioLogado.getCodigoUsuario(),
                dto.getSenhaAtual(),
                dto.getNovaSenha()
        );
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<UsuarioResponseDTO> me(@AuthenticationPrincipal Usuario usuario) {
        return ResponseEntity.ok(UsuarioResponseDTO.fromEntity(usuario));
    }
}