package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.contrato.model.Contrato;
import com.zyloerp.shared.enums.StatusContrato;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ContratoRepository extends JpaRepository<Contrato, Long> {

    Optional<Contrato> findByNumeroContrato(String numeroContrato);

    List<Contrato> findByCliente_CodigoCliente(Long codigoCliente);

    @Query("SELECT c FROM Contrato c WHERE c.cliente.codigoCliente = :codigoCliente AND c.statusContrato = 'ATIVO'")
    List<Contrato> findAtivosDoCliente(@Param("codigoCliente") Long codigoCliente);

    List<Contrato> findByStatusContrato(StatusContrato status);

    @Query("SELECT c FROM Contrato c WHERE c.statusContrato = 'ATIVO' " +
            "AND c.dataFim BETWEEN :dataInicio AND :dataFim " +
            "ORDER BY c.dataFim")
    List<Contrato> findVencendoEntre(
            @Param("dataInicio") LocalDate dataInicio,
            @Param("dataFim") LocalDate dataFim
    );

    @Query("SELECT c FROM Contrato c LEFT JOIN FETCH c.servicos WHERE c.codigoContrato = :codigo")
    Optional<Contrato> findByIdComServicos(@Param("codigo") Long codigo);

    @Query("SELECT COUNT(c) FROM Contrato c WHERE c.statusContrato = 'ATIVO'")
    Long countAtivos();
}
