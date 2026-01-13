package com.zyloerp.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "perfis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_perfil")
    private Long codigoPerfil;

    @Column(name = "nome_perfil",nullable = false, unique = true, length = 50)
    private String nomePerfil;

    @Column(name = "descricao_perfil", columnDefinition = "TEXT")
    private String descricaoPerfil;

    @Column(name = "sistema", nullable = false)
    @Builder.Default
    private Boolean sistema;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "perfil_permissoes",
            joinColumns = @JoinColumn(name = "codigo_perfil"),
            inverseJoinColumns = @JoinColumn(name = "codigo_permissao")
    )

    @Builder.Default
    private Set<Permissao> permissoes = new HashSet<>();

    public void adicionarPermissao(Permissao permissao) {
        this.permissoes.add(permissao);
    }

    public void removerPermissao(Permissao permissao) {
        this.permissoes.remove(permissao);
    }

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }

}
