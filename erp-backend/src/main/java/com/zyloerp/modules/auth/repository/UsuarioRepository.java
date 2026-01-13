package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.auth.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM Usuario u WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivos();

    @Query("SELECT DISTINCT u FROM Usuario u " +
            "LEFT JOIN FETCH u.perfil p " +
            "LEFT JOIN FETCH p.permissoes " +
            "WHERE u.excluidoEm IS NULL")
    List<Usuario> findAllAtivosComPermissoes();

    /**
     * JPQL QUERY com parâmetro: Busca usuários por perfil.
     *
     * @param codigoPerfil Código do perfil
     * @return Lista de usuários
     */
    @Query("SELECT u FROM Usuario u WHERE u.perfil.codigoPerfil = :codigoPerfil AND u.excluidoEm IS NULL")
    List<Usuario> findByPerfil(@Param("codigoPerfil") Long codigoPerfil);

    /**
     * DERIVED QUERY: Busca por nome contendo texto (LIKE).
     *
     * @param nome Parte do nome a buscar
     * @return Lista de usuários
     */
    List<Usuario> findByNomeCompletoContainingIgnoreCaseAndExcluidoEmIsNull(String nome);

    /**
     * JPQL QUERY: Busca usuários ativos ou inativos.
     *
     * @param ativo true = ativos, false = inativos
     * @return Lista de usuários
     */
    @Query("SELECT u FROM Usuario u WHERE u.ativo = :ativo AND u.excluidoEm IS NULL")
    List<Usuario> findByAtivo(@Param("ativo") Boolean ativo);
}