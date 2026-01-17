package com.zyloerp.modules.contrato.model;

import com.zyloerp.core.entity.BaseEntity;
import com.zyloerp.modules.cliente.model.Cliente;
import com.zyloerp.shared.enums.StatusContrato;
import com.zyloerp.shared.enums.TipoContrato;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @Column(name = "numero_contrato", unique = true, length = 20)
    private String numeroContrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_cliente", nullable = false)
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_contrato", nullable = false, length = 30)
    private TipoContrato tipoContrato;

    @Column(name = "valor_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal valorTotal;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_fim", nullable = false)
    private LocalDate dataFim;

    @Column(name = "duracao_meses")
    private Integer duracaoMeses;

    @Column(name = "sla_horas")
    private Integer slaHoras;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_contrato", nullable = false, length = 20)
    @Builder.Default
    private StatusContrato statusContrato = StatusContrato.ATIVO;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @OneToMany(mappedBy = "contrato", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<ContratoServico> servicos = new HashSet<>();

    public void adicionarServico(ContratoServico contratoServico) {
        servicos.add(contratoServico);
        contratoServico.setContrato(this);
    }

    public void removerServico(ContratoServico contratoServico) {
        servicos.remove(contratoServico);
        contratoServico.setContrato(null);
    }

    public boolean isAtivo() {
        return this.statusContrato == StatusContrato.ATIVO;
    }

    public void suspender() {
        this.statusContrato = StatusContrato.SUSPENSO;
    }

    public void reativar() {
        this.statusContrato = StatusContrato.ATIVO;
    }

    public void encerrar() {
        this.statusContrato = StatusContrato.ENCERRADO;
    }

    public void cancelar() {
        this.statusContrato = StatusContrato.CANCELADO;
    }
}