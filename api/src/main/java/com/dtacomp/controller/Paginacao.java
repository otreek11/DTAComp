package com.dtacomp.controller;

import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import com.dtacomp.exception.RequisicaoInvalidaException;


final class Paginacao {

    static final int TAMANHO_PADRAO = 20;
    static final int TAMANHO_MAXIMO = 50;

    private Paginacao() {}

    static Pageable de(int page, int size, String sort, Set<String> camposOrdenaveis) {
        String[] partes = sort.split(",");
        String campo = partes[0].trim();
        if (!camposOrdenaveis.contains(campo)) {
            throw new RequisicaoInvalidaException("Campo de ordenação inválido; use: " + camposOrdenaveis);
        }

        Sort.Direction direcao = Sort.Direction.DESC;
        if (partes.length > 1) {
            String d = partes[1].trim();
            if (d.equalsIgnoreCase("asc")) {
                direcao = Sort.Direction.ASC;
            } else if (!d.equalsIgnoreCase("desc")) {
                throw new RequisicaoInvalidaException("Direção de ordenação inválida; use asc ou desc");
            }
        }

        int tamanho = Math.min(Math.max(size, 1), TAMANHO_MAXIMO);
        return PageRequest.of(Math.max(page, 0), tamanho,
                Sort.by(direcao, campo).and(Sort.by(Sort.Direction.ASC, "id")));
    }
}
