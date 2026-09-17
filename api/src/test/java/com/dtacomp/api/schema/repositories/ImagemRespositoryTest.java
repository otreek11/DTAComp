package com.dtacomp.api.schema.repositories;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.ConteudoTexto;
import com.dtacomp.api.schema.classes.Imagem;


@DataJpaTest 
public class ImagemRespositoryTest {
    @Autowired 
    private ImagemRepository imagemRepository;
    @Autowired 
    private ConteudoRepository conteudoRepository;
    @Autowired 
    private ConteudoTextRepository conteudoTextoRepository;
    
    @Test 
    public void deveEncontrarImagemPeloConteudoTextoId() {
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo").tipo(Conteudo.TipoConteudo.TEXTO).build());
        ConteudoTexto conteudo_texto = conteudoTextoRepository.save(ConteudoTexto.builder().conteudo(conteudo).corpoHtml("Lorem Ipsum Dolor").build());
        Imagem imagem = imagemRepository.save(Imagem.builder().conteudoTexto(conteudo_texto).urlImage("https://exemplo.com/image.png").legenda("Teste").build());
        
        List<Imagem> encontrada = imagemRepository.findByConteudoTexto_IdConteudo(conteudo_texto.getIdConteudo());
        
        assertThat(encontrada).contains(imagem);
        assertThat(encontrada.get(0).getUrlImage()).isEqualTo("https://exemplo.com/image.png");
        assertThat(encontrada.get(0).getConteudoTexto()).isEqualTo(conteudo_texto);
    }

    @Test 
    public void naoDeveEncontrarImagemQuandoConteudoTextoNaoPossui() {
        Conteudo conteudo = conteudoRepository.save(Conteudo.builder().titulo("Artigo sem imagens").tipo(Conteudo.TipoConteudo.TEXTO).build());
        ConteudoTexto conteudoTexto = conteudoTextoRepository.save(ConteudoTexto.builder().conteudo(conteudo).corpoHtml("Lorem Ipsum Dolor").build());

        List<Imagem> encontrada = imagemRepository.findByConteudoTexto_IdConteudo(conteudoTexto.getIdConteudo());

        assertThat(encontrada).isEmpty();
    }

}
