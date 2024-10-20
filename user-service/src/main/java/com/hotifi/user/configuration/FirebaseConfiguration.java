package com.hotifi.user.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.hotifi.common.constants.ApplicationConstants;
import com.hotifi.common.services.implementations.FirebaseMessagingServiceImpl;
import com.hotifi.common.services.interfaces.IFirebaseMessagingService;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.yaml.snakeyaml.Yaml;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

import static com.hotifi.common.constants.ApplicationConstants.*;

@Configuration
public class FirebaseConfiguration {

    @Value("{google.firebase.project-name}")
    private String firebaseProjectName;

    @Bean
    public IFirebaseMessagingService firebaseMessagingService(FirebaseMessaging firebaseMessaging) {
        return new FirebaseMessagingServiceImpl(firebaseMessaging);
    }

    @Bean
    @SuppressWarnings("unchecked")
    public FirebaseMessaging firebaseMessaging() throws IOException {

        InputStream inputStream = Files.newInputStream(Paths.get(FIREBASE_SERVICE_ACCOUNT_PATH));
        Map<String, Object> yamlData = new Yaml().load(inputStream);
        Dotenv dotenv = Dotenv.configure()
                .directory(APPLICATION_ENVIRONMENT_FILE_DIRECTORY) // Specify the directory of the .env file
                .filename(APPLICATION_ENVIRONMENT_FILENAME)
                .load();

        // Extract the relevant Firebase configuration
        Map<String, Object> firebaseConfiguration = (Map<String, Object>) yamlData.get("firebase");
        String privateKey = Objects.requireNonNull(dotenv.get("FIREBASE_PRIVATE_KEY"))
                .replace("\\n", "\n");
        firebaseConfiguration.put("private_key", privateKey);

        // Convert to JSON-like structure
        String json = new ObjectMapper().writeValueAsString(firebaseConfiguration);
        InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes());

        // Load Google Credentials
        GoogleCredentials googleCredentials = GoogleCredentials.fromStream(jsonInputStream);

        FirebaseOptions firebaseOptions = FirebaseOptions
                .builder()
                .setCredentials(googleCredentials)
                .build();
        FirebaseApp app = FirebaseApp.initializeApp(firebaseOptions, firebaseProjectName);
        return FirebaseMessaging.getInstance(app);
    }

}
