package com.dtacomp.schema.classes;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;

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
    name="usuario", 
    indexes = {
        @Index(name="idx_usuario_instituicao", columnList="id_instituicao")
    }
)
@Data 
@Builder 
@NoArgsConstructor 
@AllArgsConstructor
public class Usuario {
    
    @Id 
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name="id")
    private UUID id;
    
    @Column(name="nome")
    private String nome;
    
    @Column(name="email")
    private String email;
    
    @JsonIgnore 
    @Column(name="senha_hash")
    private String senhaHash;
    
    @Column(name="perfil")
    private TipoPerfil perfil;
    
    @ManyToOne
    @JoinColumn(name="id_instituicao")
    private Instituicao instituicao;
    
    @Column(name="id_externo")
    private String idExterno;
    
    public enum TipoPerfil {
        MEMBRO(0, "membro"),
        REVISOR(1, "revisor"),
        ADMIN(2, "admin");
    
        
        private final int num;
        private final String descricao;
        
        private TipoPerfil(int num, String descricao) {
            this.num = num;
            this.descricao = descricao;
        }
    
        public int getNum() { return num; }
        public String getDescricao() { return descricao; }
    }
}