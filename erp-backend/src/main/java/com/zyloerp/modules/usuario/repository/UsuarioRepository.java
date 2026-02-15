package com.zyloerp.modules.usuario.repository;

import com.zyloerp.modules.usuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    //Busca por e-mail
    Optional<Usuario> findByEmail(String email);

    //Busca por CODIGO_USUARIO
    Optional<Usuario> findByUsuario(Long codigoUsuario);

    //Ignora e-mail com Maiusculo e Minusculo
    Optional<Usuario> findByEmailIgnoreCase(String email);

    //Verifica se e-mail existe
    boolean existsByEmail(String email);

    //Sempre que quiser usuarios que estão ativos ignorando os deletados
    @Query("SELECT u FROM Usuario u WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivos();

    //Quando preciso de dados completos como Usuario + Perfil + Perrmissão
    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivosComPermissoes();

    //Busca somente por Tipo do Perfil
    @Query("SELECT u FROM Usuario u WHERE u.perfil.codigoPerfil = :codigoPerfil AND u.excluidoEm IS NULL")
    List<Usuario> findByPerfil(@Param("codigoPerfil") Long codigoPerfil);

    List<Usuario> findByNomeCompletoContainingIgnoreCaseAndExcluidoEmIsNull(String nome);

    @Query("SELECT u FROM Usuario u WHERE u.ativo = :ativo AND u.excluidoEm IS NULL")
    List<Usuario> findByAtivo(@Param("ativo") Boolean ativo);

    // Busca com permissões carregadas
    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE u.codigoUsuario = :codigoUsuario " +
            "AND u.excluidoEm IS NULL")
    Optional<Usuario> findByIdComPermissoes(@Param("codigoUsuario") Long codigoUsuario);

    // Busca para autenticação
    @Query("SELECT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil " +
            "WHERE LOWER(u.email) = LOWER(:email) " +
            "AND u.excluidoEm IS NULL")
    Optional<Usuario> findByEmailComPerfil(@Param("email") String email);


    // Contar usuários por perfil
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.perfil.codigoPerfil = :codigoPerfil AND u.excluidoEm IS NULL")
    Long countByPerfil(@Param("codigoPerfil") Long codigoPerfil);
}