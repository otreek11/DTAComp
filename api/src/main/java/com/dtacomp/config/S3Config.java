package com.dtacomp.config;

import java.net.URI;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.checksums.RequestChecksumCalculation;
import software.amazon.awssdk.core.checksums.ResponseChecksumValidation;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Cliente S3 para MinIO (local) ou Cloudflare R2 (produção).
 * R2: endpoint https://<account-id>.r2.cloudflarestorage.com, region "auto".
 * MinIO: endpoint http://minio:9000 e path-style=true.
 */
@Configuration
@ConditionalOnProperty(name = "app.storage.tipo", havingValue = "s3")
public class S3Config {

    @Bean(destroyMethod = "close")
    public S3Client s3Client(@Value("${app.storage.s3.endpoint}") String endpoint,
                             @Value("${app.storage.s3.region:auto}") String region,
                             @Value("${app.storage.s3.access-key}") String accessKey,
                             @Value("${app.storage.s3.secret-key}") String secretKey,
                             @Value("${app.storage.s3.path-style:false}") boolean pathStyle) {
        return S3Client.builder()
                .endpointOverride(URI.create(endpoint))
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(accessKey, secretKey)))
                .forcePathStyle(pathStyle)
                // Checksums só quando exigidos: compatibilidade com R2/MinIO
                .requestChecksumCalculation(RequestChecksumCalculation.WHEN_REQUIRED)
                .responseChecksumValidation(ResponseChecksumValidation.WHEN_REQUIRED)
                .build();
    }
}
