package com.dtacomp.service.armazenamento;

import java.io.ByteArrayInputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.core.io.Resource;

import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class ArmazenamentoS3ServiceTest {

    private static final String BUCKET = "dtacomp";

    private S3Client s3;
    private ArmazenamentoS3Service service;

    @BeforeEach
    void setUp() {
        s3 = mock(S3Client.class);
        service = new ArmazenamentoS3Service(s3, BUCKET);
    }

    @Test
    void salvarEnviaObjetoComChaveGerada() throws Exception {
        ArgumentCaptor<PutObjectRequest> req = ArgumentCaptor.forClass(PutObjectRequest.class);
        ArgumentCaptor<RequestBody> body = ArgumentCaptor.forClass(RequestBody.class);

        String chave = service.salvar("imagens", "PNG", new ByteArrayInputStream(new byte[] {1, 2, 3}));

        assertTrue(chave.matches("imagens/[0-9a-f-]{36}\\.png"), chave);
        verify(s3).putObject(req.capture(), body.capture());
        assertEquals(BUCKET, req.getValue().bucket());
        assertEquals(chave, req.getValue().key());
        assertEquals(3L, body.getValue().optionalContentLength().orElseThrow());
    }

    @Test
    void salvarRejeitaPastaInvalida() {
        assertThrows(IllegalStateException.class,
                () -> service.salvar("../x", "png", new ByteArrayInputStream(new byte[0])));
        verifyNoInteractions(s3);
    }

    @Test
    void carregarDevolveConteudoDoBucket() throws Exception {
        ResponseInputStream<GetObjectResponse> stream = new ResponseInputStream<>(
                GetObjectResponse.builder().build(),
                AbortableInputStream.create(new ByteArrayInputStream(new byte[] {7, 8})));
        when(s3.getObject(any(GetObjectRequest.class))).thenReturn(stream);

        Resource recurso = service.carregar("pdf/abc.pdf");

        assertArrayEquals(new byte[] {7, 8}, recurso.getInputStream().readAllBytes());
    }

    @Test
    void carregarChaveInexistenteLancaIllegalArgument() {
        when(s3.getObject(any(GetObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("nao existe").build());

        assertThrows(IllegalArgumentException.class, () -> service.carregar("pdf/nao-existe.pdf"));
    }

    @Test
    void existeConsultaHeadObject() {
        when(s3.headObject(any(HeadObjectRequest.class))).thenReturn(HeadObjectResponse.builder().build());
        assertTrue(service.existe("imagens/a.png"));

        when(s3.headObject(any(HeadObjectRequest.class)))
                .thenThrow(NoSuchKeyException.builder().message("nao existe").build());
        assertFalse(service.existe("imagens/b.png"));
    }

    @Test
    void chavesComTravessiaSaoIgnoradas() {
        assertFalse(service.existe("../segredo"));
        assertThrows(IllegalArgumentException.class, () -> service.carregar("/etc/passwd"));
        service.remover("a/../../b");

        verify(s3, never()).headObject(any(HeadObjectRequest.class));
        verify(s3, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    @Test
    void removerApagaObjeto() {
        ArgumentCaptor<DeleteObjectRequest> req = ArgumentCaptor.forClass(DeleteObjectRequest.class);

        service.remover("imagens/a.png");

        verify(s3).deleteObject(req.capture());
        assertEquals("imagens/a.png", req.getValue().key());
    }
}
