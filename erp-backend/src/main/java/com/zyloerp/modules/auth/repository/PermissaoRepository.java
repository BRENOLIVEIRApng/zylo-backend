package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.auth.model.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissaoRepository extends JpaRepository<Permissao, Long> {

    Optional<Permissao> findByModuloAndAcao(String modulo, String acao);

    List<Permissao> findByModulo(String modulo);

    boolean existsByModuloAndAcao(String modulo, String acao);
}