package com.zyloerp.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "permissoes",
        uniqueConstraints = @UniqueConstraint(
            name = "uk_permissoes_modulo_acao",
            columnNames = {"modulo", "acao"}
        )
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permissao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_permissao")
    private Long codigoPermissao;

    @Column(name = "modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "acao", nullable = false, length = 20)
    private String acao;

    @Column(name = "descricao_permissao", columnDefinition = "TEXT")
    private String descricaoPermissao;

    public String getAuthority() {
        return this.modulo + ":" + this.acao;
    }

    public static Permissao criar(String modulo, String acao, String descricao) {
        return Permissao.builder()
                .modulo(modulo)
                .acao(acao)
                .descricaoPermissao(descricao)
                .build();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Permissao)) return false;
        Permissao that = (Permissao) o;
        return modulo.equals(that.modulo) && acao.equals(that.acao);
    }

    @Override
    public int hashCode() {
        return modulo.hashCode() + acao.hashCode();
    }
}
