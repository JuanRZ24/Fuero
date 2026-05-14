package com.lexflow.api.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.UserCredentials;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;

@Service
public class GoogleDriveService {

    @Value("${google.drive.client-id}")
    private String clientId;

    @Value("${google.drive.client-secret}")
    private String clientSecret;

    @Value("${google.drive.refresh-token}")
    private String refreshToken;

    @Value("${google.drive.folder-id}")
    private String folderId;

    private Drive getDriveService() throws Exception {
        UserCredentials credentials = UserCredentials.newBuilder()
                .setClientId(clientId)
                .setClientSecret(clientSecret)
                .setRefreshToken(refreshToken)
                .build();

        return new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                new HttpCredentialsAdapter(credentials))
                .setApplicationName("LexFlow")
                .build();
    }

    public String[] uploadToDrive(MultipartFile archivoActual) {
        try {
            Drive driveService = getDriveService();

            File fileMetadata = new File();
            fileMetadata.setName(archivoActual.getOriginalFilename());
            fileMetadata.setParents(Collections.singletonList(folderId));

            InputStreamContent mediaContent = new InputStreamContent(
                    archivoActual.getContentType(), archivoActual.getInputStream());

            File file = driveService.files().create(fileMetadata, mediaContent)
                    .setFields("id, webViewLink")
                    .execute();

            String fileId = file.getId();

            Permission permission = new Permission()
                    .setType("anyone")
                    .setRole("reader");
            driveService.permissions().create(fileId, permission).execute();

            String previewUrl = "https://drive.google.com/file/d/" + fileId + "/preview";
            
            System.out.println("✅ Archivo subido como un campeón. ID: " + fileId);

            return new String[]{fileId, previewUrl};

        } catch (Exception e) {
            System.err.println("❌ Error crítico en GoogleDriveService: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}