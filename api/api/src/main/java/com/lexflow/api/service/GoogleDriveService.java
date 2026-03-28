package com.lexflow.api.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;

@Service
public class GoogleDriveService {

    public String[] subirADrive(MultipartFile archivoActual) {
        // --- BYPASS DE DESARROLLO ---
        // Simulamos que subimos a Drive y nos devuelve datos fijos
        String fakeDriveId = "1vC6m-C-lA_I-R1mX_zGvV0RzXJz4"; // Un PDF de prueba público de Google
        String fakeUrl = "https://drive.google.com/file/d/" + fakeDriveId + "/view";
        
        System.out.println("⚠️ ALERTA: Usando bypass de Google Drive. No se subió archivo real.");
        
        return new String[]{fakeDriveId, fakeUrl};
    }
}