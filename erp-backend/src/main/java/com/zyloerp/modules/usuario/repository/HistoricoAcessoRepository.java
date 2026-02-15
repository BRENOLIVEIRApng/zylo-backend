package com.zyloerp.modules.usuario.repository;

import com.zyloerp.modules.usuario.model.HistoricoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface HistoricoAcessoRepository extends JpaRepository<HistoricoAcesso, Long> {


    List<HistoricoAcesso> findTop10ByUsuario_CodigoUsuarioOrderByDataHoraAcessoDesc(Long codigoUsuario);


    @Query("SELECT h FROM HistoricoAcesso h " +
            "WHERE h.usuario.codigoUsuario = :codigoUsuario " +
            "AND h.dataHoraAcesso BETWEEN :inicio AND :fim " +
            "ORDER BY h.dataHoraAcesso DESC")
    List<HistoricoAcesso> findByUsuarioAndPeriodo(
            @Param("codigoUsuario") Long codigoUsuario,
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    List<HistoricoAcesso> findBySucessoFalseOrderByDataHoraAcessoDesc();

    // ADICIONAR ESTES MÉTODOS NO HistoricoAcessoRepository.java

    // Acessos por período (todos os usuários)
    @Query("SELECT h FROM HistoricoAcesso h " +
            "WHERE h.dataHoraAcesso BETWEEN :inicio AND :fim " +
            "ORDER BY h.dataHoraAcesso DESC")
    List<HistoricoAcesso> findByPeriodo(
            @Param("inicio") LocalDateTime inicio,
            @Param("fim") LocalDateTime fim
    );

    // Último acesso do usuário
    @Query("SELECT h FROM HistoricoAcesso h " +
            "WHERE h.usuario.codigoUsuario = :codigoUsuario " +
            "AND h.sucesso = true " +
            "ORDER BY h.dataHoraAcesso DESC " +
            "LIMIT 1")
    Optional<HistoricoAcesso> findUltimoAcessoSucesso(@Param("codigoUsuario") Long codigoUsuario);

    // Tentativas de login por IP
    @Query("SELECT h FROM HistoricoAcesso h " +
            "WHERE h.ipAcesso = :ip " +
            "AND h.dataHoraAcesso > :dataInicio " +
            "ORDER BY h.dataHoraAcesso DESC")
    List<HistoricoAcesso> findTentativasPorIp(
            @Param("ip") String ip,
            @Param("dataInicio") LocalDateTime dataInicio
    );

    // Contar falhas recentes de um email
    @Query("SELECT COUNT(h) FROM HistoricoAcesso h " +
            "WHERE h.usuario.email = :email " +
            "AND h.sucesso = false " +
            "AND h.dataHoraAcesso > :dataInicio")
    Long countFalhasRecentes(
            @Param("email") String email,
            @Param("dataInicio") LocalDateTime dataInicio
    );


}