package com.zyloerp.core.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;

/**
 * Implementação do UserDetailsService do Spring Security.
 *
 * RESPONSABILIDADE:
 * - Carregar usuário do banco de dados pelo username (email)
 * - Usado pelo Spring Security para autenticação
 *
 * UserDetailsService:
 * - Interface do Spring Security com 1 método: loadUserByUsername()
 * - Você deve implementar este método para buscar usuário no seu banco
 */
@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    /**
     * Carrega usuário pelo username (email no nosso caso).
     *
     * CHAMADO:
     * - No login (AuthenticationManager usa internamente)
     * - No filtro JWT (para carregar usuário após validar token)
     *
     * @param username Email do usuário
     * @return UserDetails (nossa entity Usuario implementa esta interface)
     * @throws UsernameNotFoundException Se usuário não existir
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com email: " + username
                ));

        // Verifica se usuário está ativo
        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Usuário inativo ou excluído");
        }

        // Usuario implementa UserDetails, então pode retornar diretamente
        return usuario;
    }
}
