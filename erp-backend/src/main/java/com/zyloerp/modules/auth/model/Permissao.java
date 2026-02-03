package com.zyloerp.modules.auth.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "PERMISSOES",
        uniqueConstraints = @UniqueConstraint(
            name = "UK_PERMISSOES_MODULO_ACAO",
            columnNames = {"MODULO", "ACAO"}
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
    @Column(name = "CODIGO_PERMISSAO")
    private Long codigoPermissao;

    @Column(name = "MODULO", nullable = false, length = 50)
    private String modulo;

    @Column(name = "ACAO", nullable = false, length = 20)
    private String acao;

    @Column(name = "DESCRICAO_PERMISSAO", columnDefinition = "TEXT")
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
