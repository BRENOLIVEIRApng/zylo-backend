package com.zyloerp.core.security;

import com.zyloerp.modules.usuario.model.Usuario;
import com.zyloerp.modules.usuario.repository.UsuarioRepository;
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
        // MUDAR DE findByEmail PARA findByEmailComPerfil
        Usuario usuario = usuarioRepository.findByEmailComPerfil(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuário não encontrado com email: " + username
                ));

        if (!usuario.isEnabled()) {
            throw new UsernameNotFoundException("Usuário inativo ou excluído");
        }

        return usuario;
    }
}