package com.zyloerp.modules.usuario.model;

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

    @Column(name = "nome_perfil", nullable = false, unique = true, length = 50)
    private String nomePerfil;

    @Column(name = "descricao_perfil", columnDefinition = "TEXT")
    private String descricaoPerfil;

    @Column(name = "sistema", nullable = false)
    @Builder.Default
    private Boolean sistema = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    // FetchType.LAZY evita o problema de alias com EAGER + subselect no Hibernate 6
    // referencedColumnName garante que o Hibernate não infere o nome errado
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "perfil_permissoes",
            joinColumns = @JoinColumn(
                    name = "codigo_perfil",
                    referencedColumnName = "codigo_perfil"
            ),
            inverseJoinColumns = @JoinColumn(
                    name = "codigo_permissao",
                    referencedColumnName = "codigo_permissao"
            )
    )
    @Builder.Default
    private Set<Permissao> permissoes = new HashSet<>();

    public void adicionarPermissao(Permissao permissao) {
        this.permissoes.add(permissao);
        permissao.getPerfils().add(this); // bidirecional sincronizado
    }

    public void removerPermissao(Permissao permissao) {
        this.permissoes.remove(permissao);
        permissao.getPerfils().remove(this);
    }

    @PrePersist
    protected void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}