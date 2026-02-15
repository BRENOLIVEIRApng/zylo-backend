package com.zyloerp.modules.usuario.service;

import com.zyloerp.modules.usuario.exception.EmailJaExisteException;
import com.zyloerp.modules.usuario.exception.UsuarioNaoEncontradoException;
import com.zyloerp.modules.usuario.exception.ValidacaoException;
import com.zyloerp.modules.usuario.model.HistoricoAcesso;
import com.zyloerp.modules.usuario.model.Perfil;
import com.zyloerp.modules.usuario.model.Usuario;
import com.zyloerp.modules.usuario.repository.HistoricoAcessoRepository;
import com.zyloerp.modules.usuario.repository.PerfilRepository;
import com.zyloerp.modules.usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final HistoricoAcessoRepository historicoAcessoRepository;
    private final PasswordEncoder passwordEncoder;

    // CRIAR
    @Transactional
    public Usuario criarUsuario(String nomeCompleto, String email, String senha, Long codigoPerfil) {
        validarCamposObrigatorios(nomeCompleto, email, senha, codigoPerfil);
        validarSenha(senha);

        if (usuarioRepository.existsByEmail(email.toLowerCase())) {
            throw new EmailJaExisteException("E-mail já está em uso");
        }

        Perfil perfil = perfilRepository.findById(codigoPerfil)
                .orElseThrow(() -> new ValidacaoException("Perfil não encontrado"));

        Usuario usuario = Usuario.builder()
                .nomeCompleto(nomeCompleto.trim())
                .email(email.toLowerCase().trim())
                .senhaHash(passwordEncoder.encode(senha))
                .perfil(perfil)
                .ativo(true)
                .criadoEm(LocalDateTime.now())
                .build();

        return usuarioRepository.save(usuario);
    }

    // BUSCAR
    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long codigoUsuario) {
        return usuarioRepository.findById(codigoUsuario)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
    }

    @Transactional(readOnly = true)
    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmailIgnoreCase(email);
    }

    @Transactional(readOnly = true)
    public Usuario buscarComPermissoes(Long codigoUsuario) {
        return usuarioRepository.findByIdComPermissoes(codigoUsuario)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));
    }

    // LISTAR
    @Transactional(readOnly = true)
    public List<Usuario> listarAtivos() {
        return usuarioRepository.findAllAtivos();
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarPorPerfil(Long codigoPerfil) {
        return usuarioRepository.findByPerfil(codigoPerfil);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarPorStatus(Boolean ativo) {
        return usuarioRepository.findByAtivo(ativo);
    }

    @Transactional(readOnly = true)
    public List<Usuario> buscarPorNome(String nome) {
        return usuarioRepository.findByNomeCompletoContainingIgnoreCaseAndExcluidoEmIsNull(nome);
    }

    // EDITAR
    @Transactional
    public Usuario editarUsuario(Long codigoUsuario, String nomeCompleto, String email, Long codigoPerfil, Long usuarioLogado) {
        Usuario usuario = buscarPorId(codigoUsuario);

        if (nomeCompleto != null && !nomeCompleto.isBlank()) {
            usuario.setNomeCompleto(nomeCompleto.trim());
        }

        if (email != null && !email.isBlank()) {
            String emailNormalizado = email.toLowerCase().trim();
            if (!usuario.getEmail().equals(emailNormalizado) &&
                    usuarioRepository.existsByEmail(emailNormalizado)) {
                throw new EmailJaExisteException("E-mail já está em uso");
            }
            usuario.setEmail(emailNormalizado);
        }

        if (codigoPerfil != null) {
            Perfil perfil = perfilRepository.findById(codigoPerfil)
                    .orElseThrow(() -> new ValidacaoException("Perfil não encontrado"));
            usuario.setPerfil(perfil);
        }

        usuario.setAtualizadoEm(LocalDateTime.now());
        usuario.setAtualizadoPor(usuarioLogado);

        return usuarioRepository.save(usuario);
    }

    // RESETAR SENHA
    @Transactional
    public void resetarSenha(Long codigoUsuario, String novaSenha, Long usuarioLogado) {
        validarSenha(novaSenha);

        Usuario usuario = buscarPorId(codigoUsuario);
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuario.setAtualizadoPor(usuarioLogado);

        usuarioRepository.save(usuario);
    }

    @Transactional
    public void alterarSenha(Long codigoUsuario, String senhaAtual, String novaSenha) {
        validarSenha(novaSenha);

        Usuario usuario = buscarPorId(codigoUsuario);

        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new ValidacaoException("Senha atual incorreta");
        }

        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setAtualizadoEm(LocalDateTime.now());
        usuario.setAtualizadoPor(codigoUsuario);

        usuarioRepository.save(usuario);
    }

    // DESATIVAR/ATIVAR
    @Transactional
    public void desativarUsuario(Long codigoUsuario, Long usuarioLogado) {
        Usuario usuario = buscarPorId(codigoUsuario);
        usuario.desativar();
        usuario.setExcluidoEm(LocalDateTime.now());
        usuario.setExcluidoPor(usuarioLogado);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void ativarUsuario(Long codigoUsuario) {
        Usuario usuario = buscarPorId(codigoUsuario);
        usuario.reativar();
        usuarioRepository.save(usuario);
    }

    // HISTÓRICO DE ACESSO
    @Transactional
    public void registrarAcessoSucesso(String email, String ip, String userAgent) {
        Usuario usuario = buscarPorEmail(email)
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado"));

        HistoricoAcesso historico = HistoricoAcesso.loginSucesso(usuario, ip, userAgent);
        historicoAcessoRepository.save(historico);

        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public void registrarAcessoFalha(String email, String ip, String userAgent, String motivo) {
        Optional<Usuario> usuarioOpt = buscarPorEmail(email);

        if (usuarioOpt.isPresent()) {
            HistoricoAcesso historico = HistoricoAcesso.loginFalha(
                    usuarioOpt.get(), ip, userAgent, motivo);
            historicoAcessoRepository.save(historico);
        }
    }

    @Transactional(readOnly = true)
    public List<HistoricoAcesso> listarHistoricoAcesso(Long codigoUsuario) {
        return historicoAcessoRepository
                .findTop10ByUsuario_CodigoUsuarioOrderByDataHoraAcessoDesc(codigoUsuario);
    }

    @Transactional(readOnly = true)
    public List<HistoricoAcesso> listarAcessosFalhos() {
        return historicoAcessoRepository.findBySucessoFalseOrderByDataHoraAcessoDesc();
    }

    // VALIDAÇÕES
    private void validarCamposObrigatorios(String nomeCompleto, String email,
                                           String senha, Long codigoPerfil) {
        if (nomeCompleto == null || nomeCompleto.trim().isBlank()) {
            throw new ValidacaoException("Nome completo é obrigatório");
        }
        if (email == null || !email.contains("@")) {
            throw new ValidacaoException("Email inválido");
        }
        if (senha == null || senha.isBlank()) {
            throw new ValidacaoException("Senha é obrigatória");
        }
        if (codigoPerfil == null) {
            throw new ValidacaoException("Perfil é obrigatório");
        }
    }

    private void validarSenha(String senha) {
        if (senha == null || senha.length() < 8) {
            throw new ValidacaoException("Senha deve ter no mínimo 8 caracteres");
        }
    }

    // VERIFICAÇÕES
    @Transactional(readOnly = true)
    public boolean podeLogar(String email) {
        return buscarPorEmail(email)
                .map(u -> u.getAtivo() && u.getExcluidoEm() == null)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public boolean verificarSenha(String email, String senha) {
        return buscarPorEmail(email)
                .map(u -> passwordEncoder.matches(senha, u.getSenhaHash()))
                .orElse(false);
    }
}