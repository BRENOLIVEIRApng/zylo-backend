package com.zyloerp.modules.cliente.model;

import com.zyloerp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cliente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_cliente")
    private Long codigoCliente;

    @Column(name = "razao_social", nullable = false, length = 200)
    private String razaoSocial;

    @Column(name = "nome_fantasia", length = 200)
    private String nomeFantasia;

    /**
     * CNPJ formatado: 00.000.000/0000-00
     * Único entre clientes ativos (índice parcial no banco).
     */
    @Column(name = "cnpj", nullable = false, unique = true, length = 18)
    private String cnpj;

    @Column(name = "inscricao_estadual", length = 20)
    private String inscricaoEstadual;

    // ENDEREÇO
    @Column(name = "cep", length = 10)
    private String cep;

    @Column(name = "logradouro", length = 200)
    private String logradouro;

    @Column(name = "numero_endereco", length = 20)
    private String numeroEndereco;

    @Column(name = "complemento", length = 100)
    private String complemento;

    @Column(name = "bairro", length = 100)
    private String bairro;

    @Column(name = "cidade", length = 100)
    private String cidade;

    @Column(name = "estado", length = 2)
    private String estado;

    /**
     * Status do cliente: ATIVO, SUSPENSO, ENCERRADO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_cliente", nullable = false, length = 20)
    @Builder.Default
    private StatusCliente statusCliente = StatusCliente.ATIVO;

    /**
     * Contatos do cliente.
     * Carregamento LAZY para não trazer sempre (só quando necessário).
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContatoCliente> contatos = new ArrayList<>();

    /**
     * Contratos do cliente.
     */
    @OneToMany(mappedBy = "cliente")
    @Builder.Default
    private List<Contrato> contratos = new ArrayList<>();

    /**
     * Status do cliente: ATIVO, SUSPENSO, ENCERRADO
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status_cliente", nullable = false, length = 20)
    @Builder.Default
    private StatusCliente statusCliente = StatusCliente.ATIVO;

    /**
     * Contatos do cliente.
     * Carregamento LAZY para não trazer sempre (só quando necessário).
     */
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ContatoCliente> contatos = new ArrayList<>();

    /**
     * Contratos do cliente.
     */
    @OneToMany(mappedBy = "cliente")
    @Builder.Default
    private List<Contrato> contratos = new ArrayList<>();

    // MÉTODOS DE NEGÓCIO

    public void adicionarContato(ContatoCliente contato) {
        contatos.add(contato);
        contato.setCliente(this);
    }

    public void removerContato(ContatoCliente contato) {
        contatos.remove(contato);
        contato.setCliente(null);
    }

    public ContatoCliente getContatoPrincipal() {
        return contatos.stream()
                .filter(ContatoCliente::getPrincipal)
                .findFirst()
                .orElse(null);
    }

    public boolean isSuspenso() {
        return this.statusCliente == StatusCliente.SUSPENSO;
    }

    public void suspender() {
        this.statusCliente = StatusCliente.SUSPENSO;
    }

    public void reativar() {
        this.statusCliente = StatusCliente.ATIVO;
    }

    public void encerrar() {
        this.statusCliente = StatusCliente.ENCERRADO;
    }

    enum StatusCliente {
        ATIVO,      // Cliente operando normalmente
        SUSPENSO,   // Temporariamente inativo (sem novos contratos)
        ENCERRADO   // Relação comercial finalizada
    }
}
