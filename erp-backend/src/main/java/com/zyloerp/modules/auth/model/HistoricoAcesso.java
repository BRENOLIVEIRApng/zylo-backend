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
    @Column(name = "CODIGO_ACESSO")
    private long codigoAcesso;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CODIGO_USUARIO", nullable = false)
    private Usuario usuario;

    @Column(name = "IP_ACESSO", length = 45)
    private String ipAcesso;

    @Column(name = "USER_AGENT", columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "DATA_HORA_ACESSO", nullable = false)
    private LocalDateTime dataHoraAcesso;

    @Column(name = "SUCESSO", nullable = false)
    private boolean sucesso = true;

    @Column(name = "MOTIVO_FALHA")
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
