package com.zyloerp.modules.usuario.service;

import com.zyloerp.modules.usuario.model.Usuario;
import com.zyloerp.modules.usuario.repository.UsuarioRepository;

public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    //Valida os e-mail quando criar um novo usuario
    public Usuario create(Usuario usuario) {
        if (UsuarioRepository.existsByEmail(usuario.getEmail())){
            throw new RuntimeException("E-mail já existe");
        }
        return usuarioRepository.save(usuario);
    }
    
}
