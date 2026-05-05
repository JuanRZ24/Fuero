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

    public String subirArchivo(MultipartFile archivo, Long asuntoId) {
        // 1. Sacamos el ID del despacho logueado desde nuestra bóveda de seguridad
        Long despachoId = TenantContext.getCurrentTenant();
        
        if (despachoId == null) {
            throw new RuntimeException("Acceso denegado: No se detectó un despacho activo.");
        }

        // 2. Construimos la ruta mágica (Ej. "despacho_5/asunto_10/demanda.pdf")
        String rutaArchivo = String.format("despacho_%d/asunto_%d/%s", 
                despachoId, asuntoId, archivo.getOriginalFilename());

        // 3. Preparamos el paquete para enviarlo a MinIO
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(rutaArchivo)
                    .contentType(archivo.getContentType())
                    .build();

            // 4. Lo subimos directamente desde la memoria RAM, sin tocar el disco de tu PC
            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(archivo.getInputStream(), archivo.getSize()));

            return rutaArchivo; // Devolvemos la ruta para que la guardes en tu base de datos (Ej. tabla Documentos)
            
        } catch (IOException e) {
            throw new RuntimeException("Fallo al intentar subir el archivo a MinIO", e);
        }
    }

    public String generarUrlTemporalDeDescarga(String rutaArchivoEnMinio) {
        
        // 1. Decimos qué archivo queremos
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(rutaArchivoEnMinio)
                .build();

        // 2. Configuramos las reglas del "Boleto VIP" (ej. Válido por 15 minutos)
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(15))
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Firmamos criptográficamente la petición
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        // 4. Devolvemos la URL lista para que el navegador la use
        return presignedRequest.url().toString();
    }


    public void borrarArchivo(String rutaArchivo) {
        try {
            log.info("Intentando eliminar objeto del bucket: {} con llave: {}", bucketName, rutaArchivo);
            
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(rutaArchivo)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            log.info("Archivo eliminado correctamente de MinIO: {}", rutaArchivo);
        } catch (Exception e) {
            // Aquí lanzamos una excepción personalizada o logueamos el error crítico
            log.error("Error al eliminar archivo en MinIO: {}", rutaArchivo, e);
            throw new RuntimeException("No se pudo eliminar el archivo físico del storage", e);
        }
    }
}