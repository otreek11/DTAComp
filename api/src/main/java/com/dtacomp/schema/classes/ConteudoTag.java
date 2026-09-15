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
@Table(name="conteudo_tag")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class ConteudoTag {
    @EmbeddedId 
    private ConteudoTagPK pk;    

    @ManyToOne 
    @JoinColumn(name="id_tag")
    private Tag tag;
    
    @ManyToOne 
    @JoinColumn(name="id_conteudo")
    private Conteudo conteudo;

    @Embeddable 
    class ConteudoTagPK implements Serializable {
        @Column(name="id_conteudo")
        UUID idConteudo;

        @Column(name="id_tag")
        UUID idTag;
    }
}
