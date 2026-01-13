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
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @CreatedBy
    @Column(name = "criado_por", nullable = false, updatable = false)
    private Long criadoPor;

    @LastModifiedDate
    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    @LastModifiedBy
    @Column(name = "atualizado_por", nullable = false)
    private Long atualizadoPor;

    @Column(name = "excluido_em")
    private LocalDateTime excluidoEm;

    @Column(name = "excluido_por")
    private Long excluidoPor;

    public boolean isExcluidoEm() {
        return this.excluidoEm != null;
    }

    public void reativar(){ //Não salva diretamente no banco, precisa chamar repository.save()
        this.excluidoEm = null;
        this.excluidoPor = null;
    }
}
