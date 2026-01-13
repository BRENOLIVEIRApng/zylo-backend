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
    @Column(name = "codigo_contato")
    private Long codigoContato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_cliente", nullable = false)
    private Cliente cliente;

    @Column(name = "nome_contato", nullable = false, length = 100)
    private String nome;

    @Column(name = "email_contato", length = 100)
    private String emailContato;

    @Column(name = "telefone", length = 20)
    private String telefone;

    @Column(name = "celular", length = 20)
    private String celular;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private boolean ativo = true;
}
