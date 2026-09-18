package com.dtacomp.api.schema.classes;

import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="conteudo_texto")
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class ConteudoTexto {
    
    @Id
    @Column(name = "id_conteudo")
    private UUID idConteudo;

    @OneToOne
    @MapsId
    @JoinColumn(name = "id_conteudo")
    private Conteudo conteudo;

    @OneToMany(mappedBy = "conteudoTexto")
    private Set<Imagem> imagens;

    @Column(name = "corpo_html")
    private String corpoHtml;
}
