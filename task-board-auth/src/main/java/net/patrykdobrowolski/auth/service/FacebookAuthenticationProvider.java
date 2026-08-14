package net.patrykdobrowolski.auth.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class FacebookAuthenticationProvider implements OAuth2AuthenticationProvider {

    public static final String USER_ENDPOINT = "https://graph.facebook.com/me";
    public static final String TOKEN_ENDPOINT = "https://graph.facebook.com/v19.0/oauth/access_token";

    @Value("${oauth2.facebook.client-id}")
    private String clientId;

    @Value("${oauth2.facebook.client-secret}")
    private String clientSecret;

    @Value("${oauth2.redirect-uri-base}")
    private String redirectUriBase;

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.facebook;
    }

    private record FacebookTokenResponse(
            String access_token,
            String token_type,
            Integer expires_in
    ) {}

    private record FacebookUserResponse(
            String id,
            String name,
            String email
    ) {}

    @Override
    public ExternalUserProfile authenticate(String tokenString) {
        String accessToken = retrieveAccessToken(tokenString);
        FacebookUserResponse userResponse = retrieveUserProfile(accessToken);
        String displayName = userResponse.name() != null ? userResponse.name() : "Użytkownik Facebook";
        return new ExternalUserProfile(
                userResponse.id(),
                userResponse.id(),
                displayName
        );
    }

    private @NonNull FacebookUserResponse retrieveUserProfile(String accessToken) {
        String userUrl = UriComponentsBuilder.fromUriString(USER_ENDPOINT)
                .queryParam("fields", "id,name,email")
                .toUriString();
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        userHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<FacebookUserResponse> userResponseEntity = restTemplate.exchange(
                userUrl,
                HttpMethod.GET,
                userRequest,
                FacebookUserResponse.class
        );
        FacebookUserResponse userResponse = userResponseEntity.getBody();
        if (userResponse == null || userResponse.id() == null) {
            throw new RuntimeException("Nie udało się pobrać profilu użytkownika z Facebooka");
        }
        return userResponse;
    }

    private @NonNull String retrieveAccessToken(String tokenString) {
        String tokenUrl = UriComponentsBuilder.fromUriString(TOKEN_ENDPOINT)
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("redirect_uri", redirectUriBase + AuthProvider.facebook.name())
                .queryParam("code", tokenString)
                .toUriString();
        ResponseEntity<FacebookTokenResponse> tokenResponseEntity = restTemplate.getForEntity(
                tokenUrl,
                FacebookTokenResponse.class
        );
        FacebookTokenResponse tokenResponse = tokenResponseEntity.getBody();
        if (tokenResponse == null || tokenResponse.access_token() == null) {
            throw new RuntimeException("Nie udało się pobrać access_token z Facebooka");
        }
        return tokenResponse.access_token();
    }
}
