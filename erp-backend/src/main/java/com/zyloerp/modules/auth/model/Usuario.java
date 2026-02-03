package com.zyloerp.modules.auth.model;

import com.zyloerp.core.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario extends BaseEntity implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "CODIGO_USUARIO")
    private Long codigoUsuario;

    @Column(name = "NOME_COMPLETO", nullable = false, length = 100)
    private String nomeCompleto;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 100)
    private String email;

    @Column(name = "SENHA_HASH", nullable = false, length = 255)
    private String senhaHash;

    @Column(name = "ATIVO", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CODIGO_PERFIL", nullable = false)
    private Perfil perfil;

    @OneToMany(mappedBy = "USUARIO", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistoricoAcesso> historicoAcessos = new ArrayList<>();

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.senhaHash;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        if (this.perfil != null && this.perfil.getPermissoes() != null) {
            for (Permissao permissao : this.perfil.getPermissoes()) {
                String authority = permissao.getModulo() + ":" + permissao.getAcao();
                authorities.add(new SimpleGrantedAuthority(authority));
            }
        }

        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return this.isExcluidoEm();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo && this.isExcluidoEm();
    }

    public boolean temPermissao(String modulo, String acao) {
        if (this.perfil == null || this.perfil.getPermissoes() == null) {
            return false;
        }

        return this.perfil.getPermissoes().stream()
                .anyMatch(p -> p.getModulo().equals(modulo) && p.getAcao().equals(acao));
    }

    public boolean isAdmin() {
        return this.perfil != null && "Admin".equalsIgnoreCase(this.perfil.getNomePerfil());
    }

    public String getNomePerfil() {
        return this.perfil != null ? this.perfil.getNomePerfil() : "Sem perfil";
    }

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }
}