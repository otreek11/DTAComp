package com.dtacomp.service;

import org.springframework.core.io.Resource;

public record ArquivoConteudo(Resource recurso, String nome) {}
