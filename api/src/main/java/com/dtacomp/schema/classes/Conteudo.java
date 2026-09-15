package com.dtacomp.schema.classes;

import java.time.LocalDate;
import java.util.Set;
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
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(
    name="conteudo",
    indexes = {
        @Index(name="idx_conteudo_status", columnList="status"),
        @Index(name="idx_conteudo_tipo", columnList="tipo"),
    }
)
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor 
public class Conteudo {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;
    
    @Column(name="titulo")
    private String titulo;

    @Column(name="resumo")
    private String resumo;

    @Column(name="tipo")
    private TipoConteudo tipo;

    @Column(name="status")
    private StatusConteudo status;

    @Column(name="data")
    @Convert(converter = Jsr310JpaConverters.LocalDateConverter.class)
    private LocalDate data;

    @Column(name="id_externo")
    private String idExterno;

    @OneToMany 
    @JoinTable(
        name="conteudo_tag",
        joinColumns = @JoinColumn(name="id_conteudo"),
        inverseJoinColumns = @JoinColumn(name="id_tag")
    )
    private Set<Tag> tags;

    @OneToMany(mappedBy = "conteudo")
    private Set<Revisao> revisoes;
    
    public enum TipoConteudo {
        TEXTO(0, "texto"),
        PDF(1, "pdf");

        
        private final int num;
        private final String description;
        
        private TipoConteudo(int num, String description) {
            this.num = num;
            this.description = description;
        }

        public int getNum() { return num; }
        public String getDescription() { return description; }
    }

    public enum StatusConteudo {
        REVISAO(0, "revisao"),
        DEVOLVIDO(1, "devolvido"),
        PUBLICADO(2, "publicado");

        
        private final int num;
        private final String description;
        
        private StatusConteudo(int num, String description) {
            this.num = num;
            this.description = description;
        }

        public int getNum() { return num; }
        public String getDescription() { return description; }
    }
}
