package com.dtacomp.api.schema.classes;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(
    name="imagem",
    indexes = {
        @Index(name="idx_imagem_conteudo_texto", columnList="id_conteudo_texto")
    }
)
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Imagem {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;
    
    @ManyToOne 
    @JoinColumn(name="id_conteudo_texto")
    private ConteudoTexto conteudoTexto;

    @Column(name="url_image")
    private String urlImage;
    
    @Column(name="legenda")
    private String legenda;
}
