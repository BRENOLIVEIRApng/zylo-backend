package com.zyloerp.modules.servico.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Servico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_servico")
    private Long codigoServico;

    @Column(name = "nome_servico", nullable = false, length = 200)
    private String nomeServico;

    @Column(name = "descricao_servico", columnDefinition = "TEXT")
    private String descricaoServico;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_cobranca", nullable = false, length = 30)
    private TipoCobranca tipoCobranca;

    @Column(name = "valor_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorBase;

    @Column(name = "sla_horas")
    private Integer slaHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", length = 30)
    private CategoriaServico categoria;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;
}

enum TipoCobranca {
    MENSALIDADE,
    PROJETO_FECHADO,
    HORAS_CONTRATADAS,
    SOB_DEMANDA
}

enum CategoriaServico {
    DESENVOLVIMENTO,
    SUPORTE,
    CONSULTORIA,
    INFRAESTRUTURA
}
