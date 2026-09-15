package com.dtacomp.schema.classes;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

    @OneToOne 
    @JoinColumn(name="id_conteudo")
    private Conteudo conteudo;

    @OneToOne 
    @JoinColumn(name="id_revisor")
    private Usuario revisor;

    @Column(name="status")
    private Conteudo.StatusConteudo status;
}
