package com.zyloerp.modules.usuario.dto;

import com.zyloerp.modules.usuario.model.HistoricoAcesso;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistoricoAcessoResponseDTO {

    private Long codigoAcesso;
    private Long codigoUsuario;
    private String ipAcesso;
    private String userAgent;
    private LocalDateTime dataHoraAcesso;
    private boolean sucesso;
    private String motivoFalha;

    public static HistoricoAcessoResponseDTO fromEntity(HistoricoAcesso historico) {
        return HistoricoAcessoResponseDTO.builder()
                .codigoAcesso(historico.getCodigoAcesso())
                .codigoUsuario(historico.getUsuario().getCodigoUsuario())
                .ipAcesso(historico.getIpAcesso())
                .userAgent(historico.getUserAgent())
                .dataHoraAcesso(historico.getDataHoraAcesso())
                .sucesso(historico.isSucesso())
                .motivoFalha(historico.getMotivoFalha())
                .build();
    }
}