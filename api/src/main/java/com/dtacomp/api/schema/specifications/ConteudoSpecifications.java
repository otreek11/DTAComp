package com.dtacomp.api.schema.specifications;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.jpa.domain.Specification;

import com.dtacomp.api.schema.classes.Conteudo;
import com.dtacomp.api.schema.classes.Conteudo.StatusConteudo;
import com.dtacomp.api.schema.classes.Conteudo.TipoConteudo;
import com.dtacomp.api.schema.classes.Tag;
import com.dtacomp.api.schema.classes.UsuarioConteudo;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ConteudoSpecifications {

    private static final char ESCAPE = '\\';

    private ConteudoSpecifications() {}

    public static Specification<Conteudo> comStatus(StatusConteudo status) {
        if (status == null) return todos();
        return (root, query, cb) -> cb.equal(root.get("status"), status);
    }

    public static Specification<Conteudo> comTipo(TipoConteudo tipo) {
        if (tipo == null) return todos();
        return (root, query, cb) -> cb.equal(root.get("tipo"), tipo);
    }

    public static Specification<Conteudo> tituloContem(String texto) {
        if (vazio(texto)) return todos();
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("titulo")), padrao(texto), ESCAPE);
    }

    public static Specification<Conteudo> resumoContem(String texto) {
        if (vazio(texto)) return todos();
        return (root, query, cb) ->
                cb.like(cb.lower(root.get("resumo")), padrao(texto), ESCAPE);
    }

    public static Specification<Conteudo> textoLivre(String texto) {
        if (vazio(texto)) return todos();
        return (root, query, cb) -> cb.or(
                cb.like(cb.lower(root.get("titulo")), padrao(texto), ESCAPE),
                cb.like(cb.lower(root.get("resumo")), padrao(texto), ESCAPE));
    }

    public static Specification<Conteudo> dataDe(LocalDate de) {
        if (de == null) return todos();
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.<LocalDate>get("data"), de);
    }

    public static Specification<Conteudo> dataAte(LocalDate ate) {
        if (ate == null) return todos();
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.<LocalDate>get("data"), ate);
    }

    public static Specification<Conteudo> comAlgumaTag(List<String> nomes) {
        if (nomes == null || nomes.isEmpty()) return todos();
        List<String> minusculos = nomes.stream()
                .filter(n -> n != null && !n.isBlank())
                .map(n -> n.trim().toLowerCase(Locale.ROOT))
                .toList();
        if (minusculos.isEmpty()) return todos();

        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<Conteudo> correlacionado = sub.correlate(root);
            Join<Conteudo, Tag> tag = correlacionado.join("tags");
            sub.select(cb.literal(1)).where(cb.lower(tag.<String>get("nome")).in(minusculos));
            return cb.exists(sub);
        };
    }

    public static Specification<Conteudo> autorNomeContem(String nome) {
        if (vazio(nome)) return todos();
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<UsuarioConteudo> uc = sub.from(UsuarioConteudo.class);
            sub.select(cb.literal(1)).where(
                    cb.equal(uc.get("conteudo").get("id"), root.get("id")),
                    cb.like(cb.lower(uc.get("usuario").<String>get("nome")), padrao(nome), ESCAPE));
            return cb.exists(sub);
        };
    }

    public static Specification<Conteudo> comAutor(UUID idAutor) {
        if (idAutor == null) return todos();
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<UsuarioConteudo> uc = sub.from(UsuarioConteudo.class);
            sub.select(cb.literal(1)).where(
                    cb.equal(uc.get("conteudo").get("id"), root.get("id")),
                    cb.equal(uc.get("usuario").get("id"), idAutor));
            return cb.exists(sub);
        };
    }

    public static Specification<Conteudo> semAutor(UUID idAutor) {
        if (idAutor == null) return todos();
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<UsuarioConteudo> uc = sub.from(UsuarioConteudo.class);
            sub.select(cb.literal(1)).where(
                    cb.equal(uc.get("conteudo").get("id"), root.get("id")),
                    cb.equal(uc.get("usuario").get("id"), idAutor));
            return cb.not(cb.exists(sub));
        };
    }

    public static Specification<Conteudo> comAutorDaInstituicao(UUID idInstituicao) {
        if (idInstituicao == null) return todos();
        return (root, query, cb) -> {
            Subquery<Integer> sub = query.subquery(Integer.class);
            Root<UsuarioConteudo> uc = sub.from(UsuarioConteudo.class);
            sub.select(cb.literal(1)).where(
                    cb.equal(uc.get("conteudo").get("id"), root.get("id")),
                    cb.equal(uc.get("usuario").get("instituicao").get("id"), idInstituicao));
            return cb.exists(sub);
        };
    }

    public static Specification<Conteudo> todos() {
        return (root, query, cb) -> cb.conjunction();
    }

    private static boolean vazio(String s) {
        return s == null || s.isBlank();
    }

    private static String padrao(String texto) {
        String escapado = texto.trim().toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
        return "%" + escapado + "%";
    }
}
