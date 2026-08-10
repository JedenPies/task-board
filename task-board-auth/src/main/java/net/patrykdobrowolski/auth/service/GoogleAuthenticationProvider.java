package net.patrykdobrowolski.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;

@Service
public class GoogleAuthenticationProvider implements OAuth2AuthenticationProvider {

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.GOOGLE;
    }

    @Override
    public ExternalUserProfile authenticate(String tokenString) throws GeneralSecurityException, IOException {

        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                .setAudience(Collections.singletonList(clientId))
                .build();
        GoogleIdToken idToken = verifier.verify(tokenString);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            String email = payload.get("email").toString();
            String name = payload.get("name").toString();
            String googleUserId = payload.getSubject();

            return new ExternalUserProfile(googleUserId, "google:" + email, email, name);
        }
        throw new RuntimeException("Invalid token");
    }
}
