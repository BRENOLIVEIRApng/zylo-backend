package com.zyloerp.core.security;

import com.zyloerp.modules.auth.model.Usuario;
import com.zyloerp.modules.auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com email: " + username
                ));

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Usuário inativo ou excluído");
        }

        return usuario;
    }
}