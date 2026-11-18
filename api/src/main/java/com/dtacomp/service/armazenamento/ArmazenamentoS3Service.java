package com.dtacomp.service.armazenamento;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/**
 * Armazenamento em bucket compatível com S3: MinIO no ambiente local (compose.yaml)
 * e Cloudflare R2 em produção. Ativado com app.storage.tipo=s3.
 *
 * Os arquivos continuam sendo servidos pela API (o bucket pode ser privado).
 */
@Service
@ConditionalOnProperty(name = "app.storage.tipo", havingValue = "s3")
public class ArmazenamentoS3Service implements ArmazenamentoService {

    private final S3Client s3;
    private final String bucket;

    public ArmazenamentoS3Service(S3Client s3, @Value("${app.storage.s3.bucket}") String bucket) {
        this.s3 = s3;
        this.bucket = bucket;
    }

    @Override
    public String salvar(String pasta, String extensao, InputStream conteudo) {
        String chave = ChavesArmazenamento.gerar(pasta, extensao);
        byte[] bytes;
        try {
            // Os tamanhos já são limitados antes (imagens 5 MB, PDFs 20 MB).
            bytes = conteudo.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Falha ao ler arquivo", e);
        }
        s3.putObject(PutObjectRequest.builder().bucket(bucket).key(chave).build(), RequestBody.fromBytes(bytes));
        return chave;
    }

    @Override
    public Resource carregar(String chave) {
        if (!ChavesArmazenamento.valida(chave)) {
            throw new IllegalArgumentException("Arquivo não encontrado");
        }
        try {
            return new InputStreamResource(s3.getObject(GetObjectRequest.builder().bucket(bucket).key(chave).build()));
        } catch (NoSuchKeyException e) {
            throw new IllegalArgumentException("Arquivo não encontrado");
        }
    }

    @Override
    public boolean existe(String chave) {
        if (!ChavesArmazenamento.valida(chave)) {
            return false;
        }
        try {
            s3.headObject(HeadObjectRequest.builder().bucket(bucket).key(chave).build());
            return true;
        } catch (NoSuchKeyException e) {
            return false;
        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public void remover(String chave) {
        if (!ChavesArmazenamento.valida(chave)) {
            return;
        }
        s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(chave).build());
    }
}
