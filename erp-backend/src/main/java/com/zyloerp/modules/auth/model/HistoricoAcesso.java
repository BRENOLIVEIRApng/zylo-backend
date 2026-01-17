package com.zyloerp.modules.auth.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico_acessos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistoricoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "codigo_acesso")
    private long codigoAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_usuario", nullable = false)
    private Usuario usuario;

    @Column(name = "ip_acesso", length = 45)
    private String ipAcesso;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "data_hora_acesso", nullable = false)
    private LocalDateTime dataHoraAcesso;

    @Column(name = "sucesso", nullable = false)
    private boolean sucesso = true;

    @Column(name = "motivo_falha")
    private String motivoFalha;

    public static HistoricoAcesso loginSucesso(Usuario usuario, String ip, String userAgent){
        return HistoricoAcesso.builder()
                .usuario(usuario)
                .ipAcesso(ip)
                .userAgent(userAgent)
                .sucesso(true)
                .dataHoraAcesso(LocalDateTime.now())
                .build();
    }

    public static HistoricoAcesso loginFalha(Usuario usuario, String ip, String userAgent, String motivo){
        return HistoricoAcesso.builder()
                .usuario(usuario)
                .ipAcesso(ip)
                .userAgent(userAgent)
                .sucesso(false)
                .motivoFalha(motivo)
                .dataHoraAcesso(LocalDateTime.now())
                .build();
    }
}
