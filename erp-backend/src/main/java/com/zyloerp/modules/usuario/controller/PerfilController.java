package com.zyloerp.modules.usuario.controller;

import com.zyloerp.modules.usuario.dto.*;
import com.zyloerp.modules.usuario.model.Perfil;
import com.zyloerp.modules.usuario.service.PerfilService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/perfis")
@RequiredArgsConstructor
public class PerfilController {

    private final PerfilService perfilService;

    //CRIAR PERFIL
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIOS:CRIAR')")
    public ResponseEntity<PerfilResponseDTO> criar(@Valid @RequestBody PerfilRequestDTO dto) {
        Perfil perfil = perfilService.criarPerfil(
                dto.getNomePerfil(),
                dto.getDescricaoPerfil(),
                dto.getPermissoesIds()
        );
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(PerfilResponseDTO.fromEntity(perfil));
    }

    //LISTAR PERFIS
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<PerfilResponseDTO>> listarTodos() {
        List<PerfilResponseDTO> perfis = perfilService.listarTodos()
                .stream()
                .map(PerfilResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(perfis);
    }

    //LISTAR PERFIS PERSONALIZADOS
    @GetMapping("/personalizados")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<PerfilResponseDTO>> listarPersonalizados() {
        List<PerfilResponseDTO> perfis = perfilService.listarPerfisPersonalizados()
                .stream()
                .map(PerfilResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(perfis);
    }

    //LISTAR PERFIS DO SISTEMA
    @GetMapping("/sistema")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<PerfilResponseDTO>> listarSistema() {
        List<PerfilResponseDTO> perfis = perfilService.listarPerfisSistema()
                .stream()
                .map(PerfilResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(perfis);
    }

    //BUSCAR PERFIL POR ID
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<PerfilResponseDTO> buscarPorId(@PathVariable Long id) {
        Perfil perfil = perfilService.buscarComPermissoes(id);
        return ResponseEntity.ok(PerfilResponseDTO.fromEntity(perfil));
    }

    //EDITAR PERFIL
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<PerfilResponseDTO> editar(
            @PathVariable Long id,
            @Valid @RequestBody PerfilUpdateDTO dto
    ) {
        Perfil perfil = perfilService.editarPerfil(
                id,
                dto.getNomePerfil(),
                dto.getDescricaoPerfil()
        );
        return ResponseEntity.ok(PerfilResponseDTO.fromEntity(perfil));
    }

    //EXCLUIR PERFIL
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIOS:EXCLUIR')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {
        perfilService.excluirPerfil(id);
        return ResponseEntity.noContent().build();
    }

    //ADICIONAR PERMISSÃO AO PERFIL
    @PostMapping("/{id}/permissoes/{codigoPermissao}")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<Void> adicionarPermissao(
            @PathVariable Long id,
            @PathVariable Long codigoPermissao
    ) {
        perfilService.adicionarPermissao(id, codigoPermissao);
        return ResponseEntity.noContent().build();
    }

    //REMOVER PERMISSÃO DO PERFIL
    @DeleteMapping("/{id}/permissoes/{codigoPermissao}")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<Void> removerPermissao(
            @PathVariable Long id,
            @PathVariable Long codigoPermissao
    ) {
        perfilService.removerPermissao(id, codigoPermissao);
        return ResponseEntity.noContent().build();
    }

    //SINCRONIZAR PERMISSÕES DO PERFIL
    @PutMapping("/{id}/permissoes")
    @PreAuthorize("hasAuthority('USUARIOS:EDITAR')")
    public ResponseEntity<Void> sincronizarPermissoes(
            @PathVariable Long id,
            @Valid @RequestBody SincronizarPermissoesDTO dto
    ) {
        perfilService.sincronizarPermissoes(id, dto.getPermissoesIds());
        return ResponseEntity.noContent().build();
    }

    //LISTAR PERMISSÕES DO PERFIL
    @GetMapping("/{id}/permissoes")
    @PreAuthorize("hasAuthority('USUARIOS:VER')")
    public ResponseEntity<List<PermissaoResponseDTO>> listarPermissoes(@PathVariable Long id) {
        Perfil perfil = perfilService.buscarComPermissoes(id);
        List<PermissaoResponseDTO> permissoes = perfil.getPermissoes()
                .stream()
                .map(PermissaoResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(permissoes);
    }
}