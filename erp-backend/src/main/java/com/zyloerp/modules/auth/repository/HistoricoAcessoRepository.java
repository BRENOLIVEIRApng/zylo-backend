package com.zyloerp.modules.auth.repository;

import com.zyloerp.modules.auth.model.HistoricoAcesso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
}