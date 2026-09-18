package com.dtacomp.api.schema.classes;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="conteudo_pdf")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class ConteudoPDF {
    
    @Id 
    @Column(name="id_conteudo")
    private UUID idConteudo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_conteudo")
    private Conteudo conteudo;

    @Column(name="url_arquivo")
    private String urlArquivo;

    @Column(name="tamanho_bytes")
    private float tamanhoBytes;
}
