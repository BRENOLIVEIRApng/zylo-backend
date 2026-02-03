package com.zyloerp.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.Builder;

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
    @Column(name = "CODIGO_PERFIL")
    private Long codigoPerfil;

    @Column(name = "NOME_PERFIL",nullable = false, unique = true, length = 50)
    private String nomePerfil;

    @Column(name = "DESCRICAO_PERFIL", columnDefinition = "TEXT")
    private String descricaoPerfil;

    @Column(name = "SISTEMA", nullable = false)
    @Builder.Default
    private Boolean sistema = false;

    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "PERFIL_PERMISSOES",
            joinColumns = @JoinColumn(name = "CODIGO_PERFIL"),
            inverseJoinColumns = @JoinColumn(name = "CODIGO_PERMISSAO")
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
