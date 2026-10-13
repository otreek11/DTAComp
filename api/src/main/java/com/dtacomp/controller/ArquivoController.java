package com.dtacomp.controller;

import com.dtacomp.dto.error.ErrorResponse;
import com.dtacomp.exception.RequisicaoInvalidaException;
import com.dtacomp.service.armazenamento.ArmazenamentoService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/arquivos/imagens")
public class ArquivoController {

    private final ArmazenamentoService armazenamentoService;

    @Value("${app.storage.max-image-size:5242880}")
    private long maxImageSize;

    private static final Pattern NOME_ARQUIVO_PATTERN = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}\\.(png|jpg|jpeg|webp)$",
            Pattern.CASE_INSENSITIVE
    );

    public ArquivoController(ArmazenamentoService armazenamentoService) {
        this.armazenamentoService = armazenamentoService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> uploadImagem(@RequestParam("arquivo") MultipartFile arquivo) {
        if (arquivo == null || arquivo.isEmpty()) {
            throw new RequisicaoInvalidaException("Arquivo vazio ou não fornecido");
        }

        if (arquivo.getSize() > maxImageSize) {
            throw new RequisicaoInvalidaException("Tamanho máximo de imagem (5MB) excedido");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || (!contentType.equals("image/png") 
                && !contentType.equals("image/jpeg") 
                && !contentType.equals("image/webp"))) {
            throw new RequisicaoInvalidaException("Apenas PNG, JPEG e WebP são aceitos");
        }

        try {
            byte[] bytes = arquivo.getBytes();
            String extensao;

            if (contentType.equals("image/png")) {
                if (bytes.length < 4 || bytes[0] != (byte) 0x89 || bytes[1] != (byte) 0x50 
                        || bytes[2] != (byte) 0x4E || bytes[3] != (byte) 0x47) {
                    throw new RequisicaoInvalidaException("O arquivo não é um PNG válido");
                }
                extensao = "png";
            } else if (contentType.equals("image/jpeg")) {
                if (bytes.length < 2 || bytes[0] != (byte) 0xFF || bytes[1] != (byte) 0xD8) {
                    throw new RequisicaoInvalidaException("O arquivo não é um JPEG válido");
                }
                extensao = "jpg";
            } else {
                if (bytes.length < 4 || bytes[0] != (byte) 0x52 || bytes[1] != (byte) 0x49 
                        || bytes[2] != (byte) 0x46 || bytes[3] != (byte) 0x46) {
                    throw new RequisicaoInvalidaException("O arquivo não é um WebP válido");
                }
                extensao = "webp";
            }

            String chave = armazenamentoService.salvar("imagens", extensao, arquivo.getInputStream());
            String nomeArquivo = chave.substring(chave.lastIndexOf("/") + 1);

            return ResponseEntity.ok(Map.of("url", "/arquivos/imagens/" + nomeArquivo));
        } catch (IOException e) {
            throw new RequisicaoInvalidaException("Falha ao processar o arquivo");
        }
    }

    @GetMapping("/{nome}")
    public ResponseEntity<?> getImagem(@PathVariable String nome) {
        if (!NOME_ARQUIVO_PATTERN.matcher(nome).matches()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ErrorResponse(
                    HttpStatus.FORBIDDEN.value(),
                    "Acesso negado",
                    "Nome de arquivo inválido",
                    Instant.now().toString()
            ));
        }

        String chave = "imagens/" + nome;
        if (!armazenamentoService.existe(chave)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
                    HttpStatus.NOT_FOUND.value(),
                    "Não encontrado",
                    "Imagem não encontrada",
                    Instant.now().toString()
            ));
        }

        Resource resource = armazenamentoService.carregar(chave);
        
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (nome.endsWith(".png")) {
            mediaType = MediaType.IMAGE_PNG;
        } else if (nome.endsWith(".jpg") || nome.endsWith(".jpeg")) {
            mediaType = MediaType.IMAGE_JPEG;
        } else if (nome.endsWith(".webp")) {
            mediaType = MediaType.parseMediaType("image/webp");
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(resource);
    }
}