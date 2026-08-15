package net.patrykdobrowolski.auth.service.auth_provider;

import lombok.RequiredArgsConstructor;
import net.patrykdobrowolski.auth.domain.AuthProvider;
import net.patrykdobrowolski.auth.domain.exception.InvalidCredentialsException;
import net.patrykdobrowolski.auth.domain.ExternalUserProfile;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GoogleAuthenticationProvider implements OAuth2AuthenticationProvider {

    public static final String USER_INFO_ENDPOINT = "https://www.googleapis.com/oauth2/v3/userinfo";
    public static final String TOKEN_ENDPOINT = "https://oauth2.googleapis.com/token";
    public static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";

    @Value("${oauth2.google.client-id}")
    private String clientId;

    @Value("${oauth2.google.client-secret}")
    private String clientSecret;

    @Value("${oauth2.redirect-uri-base}")
    private String redirectUriBase;

    private final RestTemplate restTemplate;

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.google;
    }

    private record GoogleTokenRequest(String client_id, String client_secret, String code, String grant_type, String redirect_uri) {}
    private record GoogleTokenResponse(String access_token) {}
    private record GoogleProfileResponse(String sub, String email, String name) {}
    
    @Override
    public ExternalUserProfile authenticate(String tokenString) throws InvalidCredentialsException {

        String accessToken = retrieveAccessToken(tokenString);
        GoogleProfileResponse profileData = retrieveUserInfo(accessToken);
        return ExternalUserProfile.builder()
                .userId(profileData.sub())
                .username(profileData.email())
                .name(profileData.name())
                .build();
    }

    private @NonNull GoogleProfileResponse retrieveUserInfo(String accessToken) throws InvalidCredentialsException {
        HttpHeaders profileHeaders = new HttpHeaders();
        profileHeaders.setBearerAuth(accessToken);
        HttpEntity<Void> profileRequest = new HttpEntity<>(profileHeaders);
        ResponseEntity<GoogleProfileResponse> profileResponse = restTemplate.exchange(
                USER_INFO_ENDPOINT,
                HttpMethod.GET,
                profileRequest,
                GoogleProfileResponse.class
        );
        GoogleProfileResponse profileData = profileResponse.getBody();
        if (profileData == null) {
            throw new InvalidCredentialsException();
        }
        return profileData;
    }

    private String retrieveAccessToken(String tokenString) throws InvalidCredentialsException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        var tokenRequestBody = new GoogleTokenRequest(
                clientId, clientSecret, tokenString, GRANT_TYPE_AUTHORIZATION_CODE, redirectUriBase + AuthProvider.google.name());
        ResponseEntity<GoogleTokenResponse> tokenResponse = restTemplate.postForEntity(
                TOKEN_ENDPOINT, new HttpEntity<>(tokenRequestBody, headers), GoogleTokenResponse.class);
        if (!tokenResponse.getStatusCode().is2xxSuccessful() || tokenResponse.getBody() == null) {
            throw new InvalidCredentialsException();
        }
        return tokenResponse.getBody().access_token();
    }
}
