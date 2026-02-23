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
    @Column(name = "codigo_usuario")
    private Long codigoUsuario;

    @Column(name = "nome_completo", nullable = false, length = 100)
    private String nomeCompleto;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "senha_hash", nullable = false, length = 255)
    private String senhaHash;

    // LAZY — permissões são carregadas via JOIN FETCH na query do UserDetailsService
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_perfil", nullable = false)
    private Perfil perfil;

    @Column(name = "ativo", nullable = false)
    @Builder.Default
    private Boolean ativo = true;

    @Column(name = "criado_em", updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "ultimo_acesso")
    private LocalDateTime ultimoAcesso;

    @OneToMany(mappedBy = "usuario", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @Builder.Default
    private List<HistoricoAcesso> historicoAcessos = new ArrayList<>();

    // ─── UserDetails ─────────────────────────────────────────────────────────────

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
        // Boolean.TRUE.equals evita NPE em campos Boolean (objeto, não primitivo)
        return Boolean.TRUE.equals(this.ativo) && getExcluidoEm() == null;
    }

    // ─── Business ────────────────────────────────────────────────────────────────

    public void ativar() {
        this.ativo = true;
    }

    public void desativar() {
        this.ativo = false;
    }

    public boolean temPermissao(String modulo, String acao) {
        if (this.perfil == null || this.perfil.getPermissoes() == null) return false;
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
        if (this.criadoEm == null) this.criadoEm = LocalDateTime.now();
        if (this.ativo == null) this.ativo = true;
    }
}