package com.zyloerp.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@EntityListeners(AuditingEntityListener.class)
public  abstract class BaseEntity {

    @CreatedDate
    @Column(name = "CRIADO_EM", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @CreatedBy
    @Column(name = "CRIADO_POR", nullable = false, updatable = false)
    private Long criadoPor;

    @LastModifiedDate
    @Column(name = "ATUALIZADO_EM", nullable = false)
    private LocalDateTime atualizadoEm;

    @LastModifiedBy
    @Column(name = "ATUALIZADO_POR", nullable = false)
    private Long atualizadoPor;

    @Column(name = "EXCLUIDO_EM")
    private LocalDateTime excluidoEm;

    @Column(name = "EXCLUIDO_POR")
    private Long excluidoPor;

    public boolean isExcluidoEm() {
        return this.excluidoEm == null;
    }

    public void reativar(){ //Não salva diretamente no banco, precisa chamar repository.save()
        this.excluidoEm = null;
        this.excluidoPor = null;
    }
}
