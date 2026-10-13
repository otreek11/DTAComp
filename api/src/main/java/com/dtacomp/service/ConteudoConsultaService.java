package com.dtacomp.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.ConteudoPDF;
import com.dtacomp.api.schema.classes.ConteudoTexto;
import com.dtacomp.config.security.SecurityService;
import com.dtacomp.dto.conteudo.AutorResponse;
import com.dtacomp.dto.conteudo.ConteudoDetalheResponse;
import com.dtacomp.dto.conteudo.ConteudoDetalheResponse.PdfBloco;
import com.dtacomp.dto.conteudo.ConteudoDetalheResponse.TextoBloco;
import com.dtacomp.dto.conteudo.ConteudoResumoResponse;
import com.dtacomp.dto.conteudo.ImagemResponse;
import com.dtacomp.dto.conteudo.ParecerResponse;
import com.dtacomp.service.armazenamento.ArmazenamentoService;


@Service
@Transactional(readOnly = true)
public class ConteudoConsultaService {

    private final ConteudoService conteudoService;
    private final ConteudoTextoService conteudoTextoService;
    private final ConteudoPDFService conteudoPDFService;
    private final ImagemService imagemService;
    private final RevisaoService revisaoService;
    private final UsuarioConteudoService usuarioConteudoService;
    private final SecurityService securityService;
    private final ArmazenamentoService armazenamento;

    public ConteudoConsultaService(ConteudoService conteudoService,
                                   ConteudoTextoService conteudoTextoService,
                                   ConteudoPDFService conteudoPDFService,
                                   ImagemService imagemService,
                                   RevisaoService revisaoService,
                                   UsuarioConteudoService usuarioConteudoService,
                                   SecurityService securityService,
                                   ArmazenamentoService armazenamento) {
        this.conteudoService = conteudoService;
        this.conteudoTextoService = conteudoTextoService;
        this.conteudoPDFService = conteudoPDFService;
        this.imagemService = imagemService;
        this.revisaoService = revisaoService;
        this.usuarioConteudoService = usuarioConteudoService;
        this.securityService = securityService;
        this.armazenamento = armazenamento;
    }

    public Page<ConteudoResumoResponse> pesquisar(ConteudoFiltro filtro, Pageable pageable) {
        Page<Conteudo> pagina = conteudoService.pesquisar(filtro, pageable);

        List<UUID> ids = pagina.getContent().stream().map(Conteudo::getId).toList();
        Map<UUID, List<AutorResponse>> autoresPorConteudo = usuarioConteudoService
                .buscarAutoresDeConteudos(ids).stream()
                .collect(Collectors.groupingBy(
                        uc -> uc.getPk().getIdConteudo(),
                        LinkedHashMap::new,
                        Collectors.mapping(uc -> AutorResponse.from(uc.getUsuario()), Collectors.toList())));

        return pagina.map(c -> ConteudoResumoResponse.from(
                c, autoresPorConteudo.getOrDefault(c.getId(), List.of())));
    }

    public ConteudoDetalheResponse detalhar(UUID id) {
        Conteudo conteudo = buscarVisivel(id);

        List<AutorResponse> autores = usuarioConteudoService.buscarAutores(id).stream()
                .map(uc -> AutorResponse.from(uc.getUsuario()))
                .toList();

        TextoBloco texto = null;
        PdfBloco pdf = null;
        if (conteudo.getTipo() == Conteudo.TipoConteudo.TEXTO) {
            ConteudoTexto conteudoTexto = conteudoTextoService.buscarPorId(id);
            List<ImagemResponse> imagens = imagemService.buscarPorConteudoTexto(id).stream()
                    .map(ImagemResponse::from)
                    .toList();
            texto = new TextoBloco(conteudoTexto.getCorpoHtml(), imagens);
        } else {
            ConteudoPDF conteudoPdf = conteudoPDFService.buscarPorId(id);
            pdf = new PdfBloco(conteudoPdf.getTamanhoBytes(), "/conteudos/" + id + "/arquivo");
        }

        List<ParecerResponse> revisoes = securityService.isAutorRevisorOuAdmin(id)
                ? revisaoService.buscarPorConteudo(id).stream().map(ParecerResponse::from).toList()
                : null;

        return ConteudoDetalheResponse.from(conteudo, autores, texto, pdf, revisoes);
    }

    public ArquivoConteudo carregarPdf(UUID id) {
        Conteudo conteudo = buscarVisivel(id);
        if (conteudo.getTipo() != Conteudo.TipoConteudo.PDF) {
            throw new IllegalArgumentException("Este conteúdo não possui arquivo PDF");
        }
        ConteudoPDF pdf = conteudoPDFService.buscarPorId(id);
        return new ArquivoConteudo(armazenamento.carregar(pdf.getUrlArquivo()), "conteudo-" + id + ".pdf");
    }

    private Conteudo buscarVisivel(UUID id) {
        Conteudo conteudo = conteudoService.buscarPorId(id);
        if (!securityService.podeVisualizarConteudo(conteudo)) {
            throw new IllegalArgumentException("Conteúdo não encontrado: " + id);
        }
        return conteudo;
    }
}
