package com.dtacomp.api.schema.classes;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity 
@Table(name="instituicao")
@Data 
@Builder
@NoArgsConstructor 
@AllArgsConstructor 
public class Instituicao {
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;
    
    @Column(name="nome")
    private String nome;

    @Column(name="sigla")
    private String sigla;
}
