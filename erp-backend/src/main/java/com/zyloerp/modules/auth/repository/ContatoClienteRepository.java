package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.cliente.model.ContatoCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ContatoClienteRepository extends JpaRepository<ContatoCliente, Long> {

    List<ContatoCliente> findByCliente_CodigoCliente(Long codigoCliente);

    Optional<ContatoCliente> findByCliente_CodigoClienteAndPrincipalTrue(Long codigoCliente);

    @Query("SELECT c FROM ContatoCliente c WHERE c.cliente.codigoCliente = :codigoCliente AND c.ativo = true")
    List<ContatoCliente> findAtivos(Long codigoCliente);
}
