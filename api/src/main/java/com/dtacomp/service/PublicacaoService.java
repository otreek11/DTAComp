package com.dtacomp.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.owasp.html.HtmlPolicyBuilder;
import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.ConteudoPDF;
import com.dtacomp.api.schema.classes.ConteudoTexto;
import com.dtacomp.api.schema.classes.Imagem;
import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.classes.Usuario;
import com.dtacomp.api.schema.classes.UsuarioConteudo;
import com.dtacomp.api.schema.classes.UsuarioConteudo.UsuarioConteudoPK;
import com.dtacomp.api.schema.repositories.ImagemRepository;
import com.dtacomp.api.schema.repositories.RevisaoRepository;
import com.dtacomp.api.schema.repositories.TagRepository;
import com.dtacomp.api.schema.repositories.UsuarioConteudoRepository;
import com.dtacomp.api.schema.repositories.UsuarioRepository;
import com.dtacomp.dto.conteudo.ConteudoPDFRequest;
import com.dtacomp.dto.conteudo.ConteudoTextoRequest;
import com.dtacomp.dto.conteudo.ImagemRequest;
import com.dtacomp.exception.ConflitoException;
import com.dtacomp.exception.RequisicaoInvalidaException;
import com.dtacomp.service.armazenamento.ArmazenamentoService;


@Service
@Transactional
public class PublicacaoService {

    private static final Logger log = LoggerFactory.getLogger(PublicacaoService.class);

    static final String PASTA_PDF = "pdf";
    private static final byte[] ASSINATURA_PDF = "%PDF-".getBytes(StandardCharsets.US_ASCII);

    /** /arquivos/imagens/<uuid>.<ext> — o grupo 1 é a chave de armazenamento. */
    private static final Pattern URL_IMAGEM = Pattern.compile(
            "^/arquivos/(imagens/[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}"
            + "\\.(?:png|jpg|jpeg|webp))$");

    /** HTML de rich-text: formatação, blocos, links, imagens e tabelas; sem script, estilo inline ou eventos. */
    private static final PolicyFactory POLITICA_HTML = Sanitizers.FORMATTING
            .and(Sanitizers.BLOCKS)
            .and(Sanitizers.LINKS)
            .and(Sanitizers.IMAGES)
            .and(Sanitizers.TABLES)
            .and(new HtmlPolicyBuilder()
                    .allowElements("pre", "code", "hr", "figure", "figcaption")
                    .toFactory());

    private final ConteudoService conteudoService;
    private final ConteudoTextoService conteudoTextoService;
    private final ConteudoPDFService conteudoPDFService;
    private final ImagemService imagemService;
    private final UsuarioConteudoService usuarioConteudoService;
    private final UsuarioRepository usuarioRepository;
    private final TagRepository tagRepository;
    private final UsuarioConteudoRepository usuarioConteudoRepository;
    private final RevisaoRepository revisaoRepository;
    private final ImagemRepository imagemRepository;
    private final ArmazenamentoService armazenamento;
    private final long tamanhoMaximoPdf;

    public PublicacaoService(ConteudoService conteudoService,
                             ConteudoTextoService conteudoTextoService,
                             ConteudoPDFService conteudoPDFService,
                             ImagemService imagemService,
                             UsuarioConteudoService usuarioConteudoService,
                             UsuarioRepository usuarioRepository,
                             TagRepository tagRepository,
                             UsuarioConteudoRepository usuarioConteudoRepository,
                             RevisaoRepository revisaoRepository,
                             ImagemRepository imagemRepository,
                             ArmazenamentoService armazenamento,
                             @Value("${app.storage.max-pdf-bytes:20971520}") long tamanhoMaximoPdf) {
        this.conteudoService = conteudoService;
        this.conteudoTextoService = conteudoTextoService;
        this.conteudoPDFService = conteudoPDFService;
        this.imagemService = imagemService;
        this.usuarioConteudoService = usuarioConteudoService;
        this.usuarioRepository = usuarioRepository;
        this.tagRepository = tagRepository;
        this.usuarioConteudoRepository = usuarioConteudoRepository;
        this.revisaoRepository = revisaoRepository;
        this.imagemRepository = imagemRepository;
        this.armazenamento = armazenamento;
        this.tamanhoMaximoPdf = tamanhoMaximoPdf;
    }

    public Conteudo submeterTexto(Usuario submissor, ConteudoTextoRequest req) {
        List<Usuario> autores = resolverAutores(req.autores(), submissor);
        Set<Tag> tags = resolverTags(req.tags());
        String html = sanitizarHtml(req.corpoHtml());
        List<ImagemValidada> imagens = validarImagens(req.imagens());

        Conteudo conteudo = conteudoService.salvar(Conteudo.builder()
                .titulo(req.titulo().trim())
                .resumo(limparResumo(req.resumo()))
                .tipo(TipoConteudo.TEXTO)
                .status(StatusConteudo.REVISAO)
                .data(LocalDate.now())
                .tags(tags)
                .build());

        ConteudoTexto texto = conteudoTextoService.salvar(
                ConteudoTexto.builder().conteudo(conteudo).corpoHtml(html).build());

        salvarAutores(conteudo, autores);
        salvarImagens(texto, imagens);
        return conteudo;
    }

    public Conteudo submeterPdf(Usuario submissor, ConteudoPDFRequest req, MultipartFile arquivo) {
        List<Usuario> autores = resolverAutores(req.autores(), submissor);
        Set<Tag> tags = resolverTags(req.tags());
        validarPdf(arquivo);

        Conteudo conteudo = conteudoService.salvar(Conteudo.builder()
                .titulo(req.titulo().trim())
                .resumo(limparResumo(req.resumo()))
                .tipo(TipoConteudo.PDF)
                .status(StatusConteudo.REVISAO)
                .data(LocalDate.now())
                .tags(tags)
                .build());

        String chave = gravarPdf(arquivo);
        conteudoPDFService.salvar(ConteudoPDF.builder()
                .conteudo(conteudo)
                .urlArquivo(chave)
                .tamanhoBytes(arquivo.getSize())
                .build());

        salvarAutores(conteudo, autores);
        return conteudo;
    }

    public Conteudo reenviarTexto(UUID id, Usuario submissor, ConteudoTextoRequest req) {
        Conteudo conteudo = conteudoService.buscarPorId(id);
        exigirDevolvido(conteudo, TipoConteudo.TEXTO);

        List<Usuario> autores = resolverAutores(req.autores(), submissor);
        Set<Tag> tags = resolverTags(req.tags());
        String html = sanitizarHtml(req.corpoHtml());
        List<ImagemValidada> imagens = validarImagens(req.imagens());

        ConteudoTexto texto = conteudoTextoService.buscarPorId(id);
        texto.setCorpoHtml(html);
        conteudoTextoService.salvar(texto);

        Set<String> chavesAntigas = imagemRepository.findByConteudoTexto_IdConteudo(id).stream()
                .map(Imagem::getUrlImage)
                .collect(Collectors.toCollection(HashSet::new));
        imagemRepository.deleteByConteudoTexto_IdConteudo(id);
        imagemRepository.flush();
        salvarImagens(texto, imagens);
        removerImagensOrfasAposCommit(chavesAntigas);

        substituirAutores(conteudo, autores);
        reenviarParaRevisao(conteudo, req.titulo(), req.resumo(), tags);
        return conteudo;
    }

    public Conteudo reenviarPdf(UUID id, Usuario submissor, ConteudoPDFRequest req, MultipartFile arquivo) {
        Conteudo conteudo = conteudoService.buscarPorId(id);
        exigirDevolvido(conteudo, TipoConteudo.PDF);

        List<Usuario> autores = resolverAutores(req.autores(), submissor);
        Set<Tag> tags = resolverTags(req.tags());

        boolean substituirArquivo = arquivo != null && !arquivo.isEmpty();
        if (substituirArquivo) {
            validarPdf(arquivo);
            ConteudoPDF pdf = conteudoPDFService.buscarPorId(id);
            String chaveAntiga = pdf.getUrlArquivo();

            String chaveNova = gravarPdf(arquivo);
            pdf.setUrlArquivo(chaveNova);
            pdf.setTamanhoBytes(arquivo.getSize());
            conteudoPDFService.salvar(pdf);
            aposCommit(() -> armazenamento.remover(chaveAntiga));
        }

        substituirAutores(conteudo, autores);
        reenviarParaRevisao(conteudo, req.titulo(), req.resumo(), tags);
        return conteudo;
    }

    public void excluir(UUID id, Usuario solicitante) {
        Conteudo conteudo = conteudoService.buscarPorId(id);

        boolean admin = solicitante.getPerfil() == Usuario.TipoPerfil.ADMIN;
        if (!admin && conteudo.getStatus() == StatusConteudo.PUBLICADO) {
            throw new ConflitoException("Conteúdo publicado só pode ser removido por um administrador");
        }

        String chavePdf = null;
        Set<String> chavesImagens = new HashSet<>();
        if (conteudo.getTipo() == TipoConteudo.TEXTO) {
            imagemRepository.findByConteudoTexto_IdConteudo(id)
                    .forEach(i -> chavesImagens.add(i.getUrlImage()));
            imagemRepository.deleteByConteudoTexto_IdConteudo(id);
            imagemRepository.flush();
            conteudoTextoService.remover(id);
        } else {
            chavePdf = conteudoPDFService.buscarPorId(id).getUrlArquivo();
            conteudoPDFService.remover(id);
        }
        usuarioConteudoRepository.deleteByConteudo_Id(id);
        revisaoRepository.deleteByConteudo_Id(id);
        conteudoService.remover(id);

        final String chavePdfFinal = chavePdf;
        if (chavePdfFinal != null) {
            aposCommit(() -> armazenamento.remover(chavePdfFinal));
        }
        removerImagensOrfasAposCommit(chavesImagens);
    }

    private void exigirDevolvido(Conteudo conteudo, TipoConteudo tipoEsperado) {
        if (conteudo.getTipo() != tipoEsperado) {
            throw new RequisicaoInvalidaException("Este conteúdo é do tipo '"
                    + conteudo.getTipo().getDescription() + "'; use o endpoint correspondente");
        }
        if (conteudo.getStatus() != StatusConteudo.DEVOLVIDO) {
            throw new ConflitoException("Só é possível editar conteúdo devolvido pela revisão");
        }
    }

    private void reenviarParaRevisao(Conteudo conteudo, String titulo, String resumo, Set<Tag> tags) {
        conteudo.setTitulo(titulo.trim());
        conteudo.setResumo(limparResumo(resumo));
        conteudo.setTags(tags);
        conteudo.setStatus(StatusConteudo.REVISAO);   // a data de submissão original é mantida
        conteudoService.salvar(conteudo);
    }

    private void salvarAutores(Conteudo conteudo, List<Usuario> autores) {
        for (int i = 0; i < autores.size(); i++) {
            Usuario autor = autores.get(i);
            UsuarioConteudoPK pk = new UsuarioConteudoPK();
            pk.setIdUsuario(autor.getId());
            pk.setIdConteudo(conteudo.getId());
            usuarioConteudoService.salvar(UsuarioConteudo.builder()
                    .pk(pk).usuario(autor).conteudo(conteudo).idxPosicao(i).build());
        }
    }

    private void substituirAutores(Conteudo conteudo, List<Usuario> autores) {
        usuarioConteudoRepository.deleteByConteudo_Id(conteudo.getId());

        usuarioConteudoRepository.flush();
        salvarAutores(conteudo, autores);
    }

    private void salvarImagens(ConteudoTexto texto, List<ImagemValidada> imagens) {
        for (ImagemValidada img : imagens) {
            imagemService.salvar(Imagem.builder()
                    .conteudoTexto(texto)
                    .urlImage(img.chave())
                    .legenda(img.legenda())
                    .build());
        }
    }

    private List<Usuario> resolverAutores(List<String> emails, Usuario submissor) {
        if (emails == null || emails.isEmpty()) {
            throw new RequisicaoInvalidaException("Informe ao menos um autor");
        }

        List<String> normalizados = emails.stream()
                .map(e -> e == null ? "" : e.trim())
                .toList();
        if (normalizados.stream().anyMatch(String::isEmpty)) {
            throw new RequisicaoInvalidaException("Email de autor não pode ser vazio");
        }

        Set<String> vistos = new HashSet<>();
        for (String email : normalizados) {
            if (!vistos.add(email.toLowerCase(Locale.ROOT))) {
                throw new RequisicaoInvalidaException("Autor duplicado: " + email);
            }
        }

        Map<String, Usuario> porEmail = usuarioRepository.findByEmailIn(normalizados).stream()
                .collect(Collectors.toMap(Usuario::getEmail, u -> u));

        List<String> ausentes = normalizados.stream().filter(e -> !porEmail.containsKey(e)).toList();
        if (!ausentes.isEmpty()) {
            throw new RequisicaoInvalidaException("Autores não cadastrados: " + String.join(", ", ausentes));
        }

        if (normalizados.stream().noneMatch(e -> e.equalsIgnoreCase(submissor.getEmail()))) {
            throw new RequisicaoInvalidaException("O usuário que envia deve constar na lista de autores");
        }

        return normalizados.stream().map(porEmail::get).toList();
    }

    private Set<Tag> resolverTags(List<String> nomes) {
        Set<Tag> tags = new LinkedHashSet<>();
        if (nomes == null) {
            return tags;
        }
        List<String> ausentes = new ArrayList<>();
        for (String nome : nomes) {
            String limpo = nome == null ? "" : nome.trim();
            if (limpo.isEmpty()) {
                throw new RequisicaoInvalidaException("Tag não pode ser vazia");
            }
            tagRepository.findByNome(limpo).ifPresentOrElse(tags::add, () -> ausentes.add(limpo));
        }
        if (!ausentes.isEmpty()) {
            throw new RequisicaoInvalidaException("Tags inexistentes: " + String.join(", ", ausentes));
        }
        return tags;
    }

    /** Sem isso, um membro injeta script que roda no navegador de quem lê. */
    private String sanitizarHtml(String html) {
        String limpo = POLITICA_HTML.sanitize(html);
        if (limpo == null || limpo.isBlank()) {
            throw new RequisicaoInvalidaException("O corpo do texto ficou vazio após a sanitização do HTML");
        }
        return limpo;
    }

    private List<ImagemValidada> validarImagens(List<ImagemRequest> imagens) {
        List<ImagemValidada> validadas = new ArrayList<>();
        if (imagens == null) {
            return validadas;
        }
        Set<String> chaves = new HashSet<>();
        for (ImagemRequest img : imagens) {
            Matcher m = URL_IMAGEM.matcher(img.url().trim());
            if (!m.matches()) {
                throw new RequisicaoInvalidaException(
                        "URL de imagem inválida; use a URL devolvida por POST /arquivos/imagens");
            }
            String chave = m.group(1);
            if (!armazenamento.existe(chave)) {
                throw new RequisicaoInvalidaException("Imagem não encontrada: " + img.url());
            }
            if (chaves.add(chave)) {
                String legenda = img.legenda() == null || img.legenda().isBlank() ? null : img.legenda().trim();
                validadas.add(new ImagemValidada(chave, legenda));
            }
        }
        return validadas;
    }

    private void validarPdf(MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RequisicaoInvalidaException("O arquivo PDF é obrigatório");
        }
        if (arquivo.getSize() > tamanhoMaximoPdf) {
            throw new RequisicaoInvalidaException("O PDF excede o tamanho máximo de "
                    + (tamanhoMaximoPdf / (1024 * 1024)) + " MB");
        }
        if (!"application/pdf".equalsIgnoreCase(arquivo.getContentType())) {
            throw new RequisicaoInvalidaException("O arquivo deve ser um PDF (application/pdf)");
        }
        try (InputStream in = arquivo.getInputStream()) {
            byte[] cabecalho = in.readNBytes(ASSINATURA_PDF.length);
            if (!java.util.Arrays.equals(cabecalho, ASSINATURA_PDF)) {
                throw new RequisicaoInvalidaException("O conteúdo do arquivo não é um PDF válido");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler o arquivo enviado", e);
        }
    }

    /** Grava o PDF e garante a remoção do arquivo se a transação falhar. */
    private String gravarPdf(MultipartFile arquivo) {
        String chave;
        try (InputStream in = arquivo.getInputStream()) {
            chave = armazenamento.salvar(PASTA_PDF, "pdf", in);
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao gravar o arquivo enviado", e);
        }
        aposRollback(() -> armazenamento.remover(chave));
        return chave;
    }

    /** Remove do armazenamento as imagens que deixaram de ser usadas por qualquer conteúdo. */
    private void removerImagensOrfasAposCommit(Set<String> chavesCandidatas) {
        List<String> orfas = chavesCandidatas.stream()
                .filter(chave -> !imagemRepository.existsByUrlImage(chave))
                .toList();
        if (!orfas.isEmpty()) {
            aposCommit(() -> orfas.forEach(armazenamento::remover));
        }
    }

    private static String limparResumo(String resumo) {
        return resumo == null || resumo.isBlank() ? null : resumo.trim();
    }

    private void aposCommit(Runnable acao) {
        registrar(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                executarSemFalhar(acao);
            }
        });
    }

    private void aposRollback(Runnable acao) {
        registrar(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status == STATUS_ROLLED_BACK) {
                    executarSemFalhar(acao);
                }
            }
        });
    }

    private void registrar(TransactionSynchronization sincronizacao) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(sincronizacao);
        }
    }

    /** Limpeza de arquivo nunca deve transformar uma operação já confirmada em erro. */
    private void executarSemFalhar(Runnable acao) {
        try {
            acao.run();
        } catch (RuntimeException e) {
            log.warn("Falha ao limpar arquivo do armazenamento", e);
        }
    }

    private record ImagemValidada(String chave, String legenda) {}
}
