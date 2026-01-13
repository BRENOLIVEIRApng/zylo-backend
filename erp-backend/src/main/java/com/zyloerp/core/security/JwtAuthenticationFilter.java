package com.zyloerp.core.security;

import com.zyloerp.modules.auth.model.Usuario;
import com.zyloerp.modules.auth.repository.UsuarioRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// ==========================================
// JWT AUTHENTICATION FILTER
// ==========================================

/**
 * Filtro que intercepta TODAS as requisições HTTP e valida o token JWT.
 *
 * FLUXO:
 * 1. Requisição chega (ex: GET /api/clientes)
 * 2. Filtro extrai token do header "Authorization: Bearer <token>"
 * 3. Valida o token com JwtTokenProvider
 * 4. Se válido, carrega o usuário e autentica no Spring Security
 * 5. Passa para o próximo filtro da cadeia
 *
 * HERDA OncePerRequestFilter:
 * - Garante que executa apenas UMA VEZ por requisição
 *
 * @NonNull nas assinaturas:
 * - Indica que o parâmetro não pode ser null (validação em tempo de compilação)
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsService userDetailsService;

    /**
     * Método principal do filtro.
     * Executado ANTES de chegar no Controller.
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        try {
            // 1. Extrai o token do header Authorization
            String jwt = extractTokenFromRequest(request);

            // 2. Valida o token
            if (jwt != null && jwtTokenProvider.validateToken(jwt)) {

                // 3. Extrai o username (email) do token
                String username = jwtTokenProvider.getUsernameFromToken(jwt);

                // 4. Carrega os detalhes do usuário do banco
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 5. Cria objeto de autenticação do Spring Security
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,           // Principal (usuário)
                                null,                  // Credentials (não precisa, já validamos)
                                userDetails.getAuthorities()  // Permissões
                        );

                // 6. Seta a autenticação no contexto do Spring Security
                // A partir daqui, o Spring sabe quem é o usuário logado
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            // Log de erro, mas não bloqueia a requisição
            System.err.println("Não foi possível setar autenticação de usuário: " + ex.getMessage());
        }

        // 7. Continua para o próximo filtro (ou Controller)
        filterChain.doFilter(request, response);
    }

    /**
     * Extrai o token JWT do header Authorization.
     *
     * FORMATO ESPERADO:
     * Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
     *
     * @param request HttpServletRequest
     * @return Token JWT (sem o "Bearer ") ou null se não encontrar
     */
    private String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        // Verifica se header existe e começa com "Bearer "
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Remove "Bearer " (7 caracteres)
        }

        return null;
    }
}
