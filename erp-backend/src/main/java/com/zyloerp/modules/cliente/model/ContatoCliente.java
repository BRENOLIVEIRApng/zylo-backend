package com.zyloerp.modules.cliente.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.repository.cdi.Eager;

@Entity
@Table(name = "contatos_cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ContatoCliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODIGO_CONTATO")
    private Long codigoContato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_CLIENTE", nullable = false)
    private Cliente cliente;

    @Column(name = "NOME_CONTATO", nullable = false, length = 100)
    private String nome;

    @Column(name = "EMAIL_CONTATO", length = 100)
    private String emailContato;

    @Column(name = "TELEFONE", length = 20)
    private String telefone;

    @Column(name = "CELULAR", length = 20)
    private String celular;

    @Column(name = "CARGO", length = 100)
    private String cargo;

    @Column(name = "PRINCIPAL")
    @Builder.Default
    private boolean principal = false;

    @Column(name = "ATIVO", nullable = false)
    @Builder.Default
    private boolean ativo = true;

    public boolean getPrincipal(){
        return false;
    }
}
