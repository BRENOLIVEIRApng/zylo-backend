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
    @Column(name = "codigo_contrato_servico")
    private Long codigoContratoServico;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_contrato", nullable = false)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_servico", nullable = false)
    private Servico servico;

    /**
     * Valor do serviço NESTE contrato (pode ser diferente do valor_base).
     */
    @Column(name = "valor_servico", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorServico;

    @Column(name = "quantidade", nullable = false)
    @Builder.Default
    private Integer quantidade = 1;
}
