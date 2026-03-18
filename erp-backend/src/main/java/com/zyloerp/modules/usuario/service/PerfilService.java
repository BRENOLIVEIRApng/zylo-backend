package com.zyloerp.modules.usuario.service;

import com.zyloerp.modules.usuario.exception.PerfilNaoEncontradoException;
import com.zyloerp.modules.usuario.exception.PerfilSistemaException;
import com.zyloerp.modules.usuario.exception.ValidacaoException;
import com.zyloerp.modules.usuario.model.Perfil;
import com.zyloerp.modules.usuario.model.Permissao;
import com.zyloerp.modules.usuario.repository.PerfilRepository;
import com.zyloerp.modules.usuario.repository.PermissaoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PerfilService {

    private final PerfilRepository perfilRepository;
    private final PermissaoRepository permissaoRepository;

    // CRIAR
    @Transactional
    public Perfil criarPerfil(String nomePerfil, String descricao, Set<Long> permissoesIds) {
        if (nomePerfil == null || nomePerfil.trim().isBlank()) {
            throw new ValidacaoException("Nome do perfil é obrigatório");
        }

        if (perfilRepository.existsByNomePerfil(nomePerfil)) {
            throw new ValidacaoException("Já existe um perfil com este nome");
        }

        Perfil perfil = Perfil.builder()
                .nomePerfil(nomePerfil.trim())
                .descricaoPerfil(descricao)
                .sistema(false)
                .criadoEm(LocalDateTime.now())
                .build();

        if (permissoesIds != null && !permissoesIds.isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findAllById(permissoesIds);
            permissoes.forEach(perfil::adicionarPermissao);
        }

        return perfilRepository.save(perfil);
    }

    //BUSCAR PERFIL POR ID
    @Transactional(readOnly = true)
    public Perfil buscarPorId(Long codigoPerfil) {
        return perfilRepository.findById(codigoPerfil)
                .orElseThrow(() -> new PerfilNaoEncontradoException("Perfil não encontrado"));
    }

    //BUSCAR PERFIL POR NOME
    @Transactional(readOnly = true)
    public Perfil buscarPorNome(String nomePerfil) {
        return perfilRepository.findByNomePerfil(nomePerfil)
                .orElseThrow(() -> new PerfilNaoEncontradoException("Perfil não encontrado"));
    }

    //BUSCAR PERFIL COM PERMISSÕES
    @Transactional(readOnly = true)
    public Perfil buscarComPermissoes(Long codigoPerfil) {
        Perfil perfil = buscarPorId(codigoPerfil);
        perfil.getPermissoes().size(); // Force lazy load
        return perfil;
    }

    //LISTAR TODOS OS PERFIS
    @Transactional(readOnly = true)
    public List<Perfil> listarTodos() {
        return perfilRepository.findAllComPermissoes();
    }

    @Transactional(readOnly = true)
    public List<Perfil> listarPerfisSistema() {
        return perfilRepository.findSistemaComPermissoes();
    }

    @Transactional(readOnly = true)
    public List<Perfil> listarPerfisPersonalizados() {
        return perfilRepository.findPersonalizadosComPermissoes();
    }

    @Transactional(readOnly = true)
    public List<Perfil> listarComPermissoes() {
        return perfilRepository.findAllComPermissoes();
    }

    // EDITAR
    @Transactional
    public Perfil editarPerfil(Long codigoPerfil, String nomePerfil, String descricao) {
        Perfil perfil = buscarPorId(codigoPerfil);

        if (perfil.getSistema()) {
            throw new PerfilSistemaException("Perfis do sistema não podem ser editados");
        }

        if (nomePerfil != null && !nomePerfil.isBlank()) {
            if (!perfil.getNomePerfil().equals(nomePerfil) &&
                    perfilRepository.existsByNomePerfil(nomePerfil)) {
                throw new ValidacaoException("Já existe um perfil com este nome");
            }
            perfil.setNomePerfil(nomePerfil.trim());
        }

        if (descricao != null) {
            perfil.setDescricaoPerfil(descricao);
        }

        return perfilRepository.save(perfil);
    }

    //GERENCIAR PERMISSÕES
    @Transactional
    public void adicionarPermissao(Long codigoPerfil, Long codigoPermissao) {
        Perfil perfil = buscarPorId(codigoPerfil);

        if (perfil.getSistema()) {
            throw new PerfilSistemaException("Permissões de perfis do sistema não podem ser alteradas");
        }

        Permissao permissao = permissaoRepository.findById(codigoPermissao)
                .orElseThrow(() -> new ValidacaoException("Permissão não encontrada"));

        perfil.adicionarPermissao(permissao);
        perfilRepository.save(perfil);
    }

    //REMOVER PERMISSÃO
    @Transactional
    public void removerPermissao(Long codigoPerfil, Long codigoPermissao) {
        Perfil perfil = buscarPorId(codigoPerfil);

        if (perfil.getSistema()) {
            throw new PerfilSistemaException("Permissões de perfis do sistema não podem ser alteradas");
        }

        Permissao permissao = permissaoRepository.findById(codigoPermissao)
                .orElseThrow(() -> new ValidacaoException("Permissão não encontrada"));

        perfil.removerPermissao(permissao);
        perfilRepository.save(perfil);
    }

    //SINCRONIZAR PERMISSÕES
    @Transactional
    public void sincronizarPermissoes(Long codigoPerfil, Set<Long> permissoesIds) {
        Perfil perfil = buscarPorId(codigoPerfil);

        if (perfil.getSistema()) {
            throw new PerfilSistemaException("Permissões de perfis do sistema não podem ser alteradas");
        }

        perfil.getPermissoes().clear();

        if (permissoesIds != null && !permissoesIds.isEmpty()) {
            List<Permissao> permissoes = permissaoRepository.findAllById(permissoesIds);
            permissoes.forEach(perfil::adicionarPermissao);
        }

        perfilRepository.save(perfil);
    }

    //EXCLUIR PERFIL
    @Transactional
    public void excluirPerfil(Long codigoPerfil) {
        Perfil perfil = buscarPorId(codigoPerfil);

        if (perfil.getSistema()) {
            throw new PerfilSistemaException("Perfis do sistema não podem ser excluídos");
        }

        perfilRepository.delete(perfil);
    }

    //VERIFICAÇÕES
    @Transactional(readOnly = true)
    public boolean temPermissao(Long codigoPerfil, String modulo, String acao) {
        Perfil perfil = buscarComPermissoes(codigoPerfil);
        return perfil.getPermissoes().stream()
                .anyMatch(p -> p.getModulo().equals(modulo) && p.getAcao().equals(acao));
    }

    //VERIFICAR SE PERFIL EXISTE
    @Transactional(readOnly = true)
    public boolean perfilExiste(String nomePerfil) {
        return perfilRepository.existsByNomePerfil(nomePerfil);
    }
}