package com.lexflow.api.service;

import com.lexflow.api.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import java.time.Duration;

import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket-name}")
    private String bucketName;

    public String uploadFile(MultipartFile archivo, Long asuntoId) {
        Long despachoId = TenantContext.getCurrentTenant();
        
        if (despachoId == null) {
            throw new RuntimeException("Acceso denegado: No se detectó un despacho activo.");
        }

        String minioPath = String.format("despacho_%d/asunto_%d/%s", 
                despachoId, asuntoId, archivo.getOriginalFilename());

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(minioPath)
                    .contentType(archivo.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));

            return minioPath; 
            
        } catch (IOException e) {
            throw new RuntimeException("Fallo al intentar subir el archivo a MinIO", e);
        }
    }

    public String generateTemporaryDownloadUrl(String rutaArchivoEnMinio) {
        
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(rutaArchivoEnMinio)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest)
                .build();

        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        return presignedRequest.url().toString();
    }


    public void deleteFile(String rutaArchivo) {
        try {
            log.info("Intentando eliminar objeto del bucket: {} con llave: {}", bucketName, rutaArchivo);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(rutaArchivo)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            log.info("Archivo eliminado correctamente de MinIO: {}", rutaArchivo);
        } catch (Exception e) {
            log.error("Error al eliminar archivo en MinIO: {}", rutaArchivo, e);
            throw new RuntimeException("No se pudo eliminar el archivo físico del storage", e);
        }
    }
}