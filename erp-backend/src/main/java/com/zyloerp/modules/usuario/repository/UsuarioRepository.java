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

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByCodigoUsuario(Long codigoUsuario);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivos();

    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivosComPermissoes();

    @Query("SELECT u FROM Usuario u WHERE u.perfil.codigoPerfil = :codigoPerfil AND u.excluidoEm IS NULL")
    List<Usuario> findByPerfil(@Param("codigoPerfil") Long codigoPerfil);

    List<Usuario> findByNomeCompletoContainingIgnoreCaseAndExcluidoEmIsNull(String nome);

    @Query("SELECT u FROM Usuario u WHERE u.ativo = :ativo AND u.excluidoEm IS NULL")
    List<Usuario> findByAtivo(@Param("ativo") Boolean ativo);

    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE u.codigoUsuario = :codigoUsuario " +
            "AND u.excluidoEm IS NULL")
    Optional<Usuario> findByIdComPermissoes(@Param("codigoUsuario") Long codigoUsuario);

    /**
     * Usada pelo UserDetailsService no login.
     * JOIN FETCH perfil + permissoes resolve LazyInitializationException ao chamar getAuthorities().
     * DISTINCT evita duplicatas causadas pelo JOIN com a coleção de permissoes.
     */
    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE LOWER(u.email) = LOWER(:email) " +
            "AND u.excluidoEm IS NULL")
    Optional<Usuario> findByEmailComPerfilEPermissoes(@Param("email") String email);

    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.perfil.codigoPerfil = :codigoPerfil AND u.excluidoEm IS NULL")
    Long countByPerfil(@Param("codigoPerfil") Long codigoPerfil);
}