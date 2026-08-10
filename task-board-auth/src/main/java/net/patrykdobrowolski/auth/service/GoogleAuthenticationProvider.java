package net.patrykdobrowolski.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationProvider implements OAuth2AuthenticationProvider {

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.GOOGLE;
    }

    // TODO refactor
    @Override
    public ExternalUserProfile authenticate(String tokenString) throws GeneralSecurityException, IOException, InvalidCredentialsException {

        String tokenEndpoint = "https://oauth2.googleapis.com/token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecret);
        params.add("code", tokenString);
        params.add("grant_type", "authorization_code");

        // PAMIĘTAJ: Ten URL musi być identyczny z tym, co użyłeś we window.location.href w Angularze
        params.add("redirect_uri", "http://localhost/callback/auth/google");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        ResponseEntity<Map> tokenResponse = restTemplate.postForEntity(tokenEndpoint, request, Map.class);

        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new InvalidCredentialsException();
        }

        String accessToken = (String) tokenResponse.getBody().get("access_token");

        // 2. Pobranie danych profilu z Google za pomocą access_token
        String userInfoEndpoint = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);

        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);
        ResponseEntity<Map> profileResponse = restTemplate.exchange(
                userInfoEndpoint,
                HttpMethod.GET,
                profileRequest,
                Map.class
        );

        Map<String, Object> profileData = profileResponse.getBody();
        if (profileData == null) {
            throw new InvalidCredentialsException();
        }

        // 3. Mapowanie danych z Google na nasz obiekt
        return ExternalUserProfile.builder()
                .userId((String) profileData.get("sub")) // Google używa 'sub' jako unikalnego ID
                .email((String) profileData.get("email"))
                .username("google:" + profileData.get("sub"))
                .name((String) profileData.get("name"))
                .build();

//        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
//                .setAudience(Collections.singletonList(clientId))
//                .build();
//        GoogleIdToken idToken = verifier.verify(tokenString);
//        if (idToken != null) {
//            GoogleIdToken.Payload payload = idToken.getPayload();
//
//            String email = payload.get("email").toString();
//            String name = payload.get("name").toString();
//            String googleUserId = payload.getSubject();
//
//            return new ExternalUserProfile(googleUserId, "google:" + email, email, name);
//        }
//        throw new RuntimeException("Invalid token");
    }
}
