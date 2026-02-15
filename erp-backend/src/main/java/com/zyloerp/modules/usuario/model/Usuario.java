package com.zyloerp.modules.usuario.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.zyloerp.core.entity.BaseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CODIGO_PERFIL", nullable = false)
    private Perfil perfil;

    @Column(name = "ATIVO", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "ULTIMO_ACESSO")
    private LocalDateTime ultimoAcesso;

    @Column(name = "CRIADO_EM")
    private LocalDateTime criadoEm;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistoricoAcesso> historicoAcessos = new ArrayList<>();

    // USERDETAILS METHODS
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
                authorities.add(new SimpleGrantedAuthority(permissao.getAuthority()));
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
        return getExcluidoEm() == null;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return this.ativo && getExcluidoEm() == null;
    }

    // BUSINESS METHODS
    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean temPermissao(String modulo, String acao) {
        if (this.perfil == null || this.perfil.getPermissoes() == null) {
            return false;
        }
        return this.perfil.getPermissoes().stream()
                .anyMatch(p -> p.getModulo().equals(modulo) && p.getAcao().equals(acao));
    }

    public boolean isAdmin() {
        return this.perfil != null && "ADMIN".equalsIgnoreCase(this.perfil.getNomePerfil());
    }

    public String getNomePerfil() {
        return this.perfil != null ? this.perfil.getNomePerfil() : "Sem perfil";
    }

    @PrePersist
    protected void onCreate() {
        if (getCriadoEm() == null) {
            setCriadoEm(LocalDateTime.now());
        }
        if (this.ativo == null) {
            this.ativo = true;
        }
    }
}