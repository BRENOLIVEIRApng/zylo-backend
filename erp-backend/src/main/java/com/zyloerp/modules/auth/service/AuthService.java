package com.zyloerp.modules.auth.service;

import com.zyloerp.core.security.JwtTokenProvider;
import com.zyloerp.modules.auth.dto.LoginRequest;
import com.zyloerp.modules.auth.dto.LoginResponse;
import com.zyloerp.modules.auth.model.HistoricoAcesso;
import com.zyloerp.modules.auth.model.Usuario;
import com.zyloerp.modules.auth.repository.HistoricoAcessoRepository;
import com.zyloerp.modules.usuario.dto.UsuarioDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final HistoricoAcessoRepository historicoAcessoRepository;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        Usuario usuario = (Usuario) authentication.getPrincipal();

        String token = jwtTokenProvider.generateToken(authentication);

        registrarAcesso(usuario, true, null);

        return LoginResponse.builder()
                .token(token)
                .tipo("Bearer")
                .expiresIn(28800000L)
                .usuario(UsuarioDTO.builder()
                        .codigoUsuario(usuario.getCodigoUsuario())
                        .nomeCompleto(usuario.getNomeCompleto())
                        .email(usuario.getEmail())
                        .perfil(usuario.getNomePerfil())
                        .ativo(usuario.getAtivo())
                        .build())
                .build();
    }

    private void registrarAcesso(Usuario usuario, boolean sucesso, String motivo) {
        HistoricoAcesso acesso = HistoricoAcesso.builder()
                .usuario(usuario)
                .sucesso(sucesso)
                .motivoFalha(motivo)
                .build();

        historicoAcessoRepository.save(acesso);
    }
}