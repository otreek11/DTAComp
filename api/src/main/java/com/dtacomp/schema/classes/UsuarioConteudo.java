package com.dtacomp.schema.classes;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="usuario_conteudo")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class UsuarioConteudo {
    @EmbeddedId 
    private UsuarioConteudoPK pk;

    @ManyToOne 
    @JoinColumn(name="id_usuario")
    private Usuario usuario;
    
    @ManyToOne 
    @JoinColumn(name="id_conteudo")
    private Conteudo conteudo;

    @Column(name="idx_posicao")
    private int idx_posicao;

    @Embeddable 
    class UsuarioConteudoPK implements Serializable {
        @Column(name="id_usuario")
        UUID idUsuario;

        @Column(name="id_conteudo")
        UUID idConteudo;
    }
}
