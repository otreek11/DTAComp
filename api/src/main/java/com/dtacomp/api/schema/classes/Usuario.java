package com.dtacomp.api.schema.classes;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

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
public class Usuario implements UserDetails {
    
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

    // -- UserDetails --

    @Override
    @jakarta.persistence.Transient
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + perfil.name()));
    }

    @Override
    @JsonIgnore
    @jakarta.persistence.Transient
    public String getPassword() {
        return senhaHash;
    }

    @Override
    @jakarta.persistence.Transient
    public String getUsername() {
        return email;
    }
    
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