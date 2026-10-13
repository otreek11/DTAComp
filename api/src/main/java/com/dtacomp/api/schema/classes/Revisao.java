package com.dtacomp.api.schema.classes;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.jpa.convert.threeten.Jsr310JpaConverters;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
    name="revisao",
    indexes = {
        @Index(name="idx_revisao_conteudo", columnList="id_conteudo"),
        @Index(name="idx_revisao_revisor", columnList="id_revisor")
    }
)
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Revisao {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne  
    @JoinColumn(name="id_conteudo")
    private Conteudo conteudo;

    @ManyToOne 
    @JoinColumn(name="id_revisor")
    private Usuario revisor;

    @Column(name="status")
    private Conteudo.StatusConteudo status;

    @Column(name="parecer_humano")
    private String parecerHumano;

    @Column(name="data_revisao")
    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    private LocalDate dataRevisao;
}
