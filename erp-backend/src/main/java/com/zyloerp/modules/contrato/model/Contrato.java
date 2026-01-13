package com.zyloerp.modules.contrato.model;

import com.zyloerp.core.entity.BaseEntity;
import com.zyloerp.modules.cliente.model.Cliente;
import com.zyloerp.modules.servico.model.Servico;
import com.zyloerp.shared.enums.StatusContrato;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;

@Entity
@Table(name = "contratos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Contrato extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_contrato")
    private Long codigoContrato;

    @Column(name = "numero_contrato", unique = true, nullable = false)
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @Column(name = "codigo_cliente", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false, length = 20)
    private TipoContrato tipoContrato;

    @Column(name = "valor_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "duracao_meses")
    private Integer duracaoMes;

    @Column(name = "sla_horas")
    private Integer slaHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_contrato", nullable = false, length = 20)
    @Builder.Default
    private StatusContrato statusContrato;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ContratoServico> servicos = new HashSet<>();

    public void adicionarServico(ContratoServico contratoServico){
        servicos.add(contratoServico);
        contratoServico.setContrato(this);
    }

    public void removerServico(ContratoServico contratoServico){
        servicos.remove(ContratoServico);
        contratoServico.setContrato(this);
    }

    public boolean isAtivo(){
        return this.statusContrato == StatusContrato.ATIVO;
    }

    public void suspender(){
        this.statusContrato = StatusContrato.SUSPENSO;
    }

    public void reativar(){
        this.statusContrato = StatusContrato.ATIVO;
    }

    public void encerrar(){
        this.statusContrato = StatusContrato.ENCERRADO;
    }

    public void cancelar(){
        this.statusContrato = StatusContrato.CANCELADO;
    }
}

enum TipoContrato {
    MENSALIDADE,
    PROJETO_FECHADO,
    HORAS_CONTRATADAS
}

enum StatusContrato {
    ATIVO,
    SUSPENSO,
    ENCERRADO,
    CANCELADO
}