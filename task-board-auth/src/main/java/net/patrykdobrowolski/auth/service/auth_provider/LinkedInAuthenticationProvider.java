package net.patrykdobrowolski.auth.service.auth_provider;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.ExternalUserProfile;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class LinkedInAuthenticationProvider implements OAuth2AuthenticationProvider {

    public static final String USER_INFO_ENDPOINT = "https://api.linkedin.com/v2/userinfo";
    public static final String ACCESS_TOKEN_ENDPOINT = "https://www.linkedin.com/oauth/v2/accessToken";

    @Value("${oauth2.linkedin.client-id}")
    private String clientId;

    @Value("${oauth2.linkedin.client-secret}")
    private String clientSecret;

    @Value("${oauth2.redirect-uri-base}")
    private String redirectUriBase;

    private final RestTemplate restTemplate;

    private record LinkedinTokenResponse(
            String access_token,
            Integer expires_in
    ) {}

    private record LinkedinUserResponse(
            String sub,
            String name,
            String email
    ) {}

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.linkedin;
    }

    @Override
    public ExternalUserProfile authenticate(String tokenString) {

        String accessToken = retrieveAccessToken(tokenString);
        LinkedinUserResponse userResponse = retrieveUserProfile(accessToken);

        String displayName = userResponse.name() != null ? userResponse.name() : "Użytkownik LinkedIn";
        return new ExternalUserProfile(
                userResponse.sub(),
                userResponse.sub(),
                displayName
        );
    }

    private @NonNull LinkedinUserResponse retrieveUserProfile(String accessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        userHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<LinkedinUserResponse> userResponseEntity = restTemplate.exchange(
                USER_INFO_ENDPOINT,
                HttpMethod.GET,
                userRequest,
                LinkedinUserResponse.class
        );
        LinkedinUserResponse userResponse = userResponseEntity.getBody();
        if (userResponse == null || userResponse.sub() == null) {
            throw new RuntimeException("Nie udało się pobrać profilu z LinkedIn");
        }
        return userResponse;
    }

    private @NonNull String retrieveAccessToken(String tokenString) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        tokenHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        MultiValueMap<String, String> tokenBody = new LinkedMultiValueMap<>();
        tokenBody.add("grant_type", "authorization_code");
        tokenBody.add("code", tokenString);
        tokenBody.add("redirect_uri", redirectUriBase + AuthProvider.linkedin.name());
        tokenBody.add("client_id", clientId);
        tokenBody.add("client_secret", clientSecret);

        HttpEntity<MultiValueMap<String, String>> tokenRequest = new HttpEntity<>(tokenBody, tokenHeaders);

        ResponseEntity<LinkedinTokenResponse> tokenResponseEntity = restTemplate.postForEntity(
                ACCESS_TOKEN_ENDPOINT,
                tokenRequest,
                LinkedinTokenResponse.class
        );
        LinkedinTokenResponse tokenResponse = tokenResponseEntity.getBody();
        if (tokenResponse == null || tokenResponse.access_token() == null) {
            throw new RuntimeException("Nie udało się pobrać access_token z LinkedIn");
        }
        return tokenResponse.access_token();
    }
}

