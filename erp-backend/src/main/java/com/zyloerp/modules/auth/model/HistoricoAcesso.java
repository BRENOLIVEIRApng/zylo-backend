package com.zyloerp.modules.auth.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Builder.Default
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
