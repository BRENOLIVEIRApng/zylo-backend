package com.zyloerp.modules.contrato.model;

import com.zyloerp.modules.servico.model.Servico;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "contrato_servicos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContratoServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODIGO_CONTRATO_SERVICO")
    private Long codigoContratoServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_CONTRATO", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_SERVICO", nullable = false)
    private Servico servico;

    @Column(name = "VALOR_SERVICO", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "QUANTIDADE", nullable = false)
    @Builder.Default
    private Integer quantidade = 1;
}
