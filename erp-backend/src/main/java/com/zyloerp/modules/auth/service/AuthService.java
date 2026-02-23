package com.zyloerp.modules.auth.service;

import com.zyloerp.core.security.JwtTokenProvider;
import com.zyloerp.modules.auth.dto.LoginRequest;
import com.zyloerp.modules.auth.dto.LoginResponse;
import com.zyloerp.modules.usuario.model.HistoricoAcesso;
import com.zyloerp.modules.usuario.model.Usuario;
import com.zyloerp.modules.usuario.repository.HistoricoAcessoRepository;
import com.zyloerp.modules.usuario.repository.UsuarioRepository;
import com.zyloerp.modules.usuario.dto.UsuarioResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final HistoricoAcessoRepository historicoAcessoRepository;
    private final UsuarioRepository usuarioRepository; // necessário para persistir ultimoAcesso

    @Transactional
    public LoginResponse login(LoginRequest request, String ip, String userAgent) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail(),
                            request.getSenha()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            Usuario usuario = (Usuario) authentication.getPrincipal();

            String token = jwtTokenProvider.generateToken(authentication);

            registrarAcessoSucesso(usuario, ip, userAgent);

            // Persiste o último acesso (sem save() anterior, isso nunca era salvo)
            usuario.setUltimoAcesso(LocalDateTime.now());
            usuarioRepository.save(usuario);

            return LoginResponse.builder()
                    .token(token)
                    .tipo("Bearer")
                    .expiresIn(28800000L) // 8h em ms
                    .usuario(UsuarioResponseDTO.fromEntity(usuario))
                    .build();

        } catch (BadCredentialsException e) {
            // Não expõe se o email existe ou não — boa prática de segurança
            throw new BadCredentialsException("Email ou senha incorretos");
        }
    }

    private void registrarAcessoSucesso(Usuario usuario, String ip, String userAgent) {
        HistoricoAcesso acesso = HistoricoAcesso.builder()
                .usuario(usuario)
                .ipAcesso(ip)
                .userAgent(userAgent)
                .dataHoraAcesso(LocalDateTime.now())
                .sucesso(true)
                .build();
        historicoAcessoRepository.save(acesso);
    }
}