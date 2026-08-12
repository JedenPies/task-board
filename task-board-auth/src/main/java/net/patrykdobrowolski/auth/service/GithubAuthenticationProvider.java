package net.patrykdobrowolski.auth.service;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class GithubAuthenticationProvider implements OAuth2AuthenticationProvider {

    public static final String USER_ENDPOINT = "https://api.github.com/user";
    public static final String ACCESS_TOKEN_ENDPOINT = "https://github.com/login/oauth/access_token";
    public static final String EMAILS_ENDPOINT = "https://api.github.com/user/emails";

    @Value("${oauth2.github.client-id}")
    private String clientId;

    @Value("${oauth2.github.client-secret}")
    private String clientSecret;

    private final RestTemplate restTemplate;

    private record GithubTokenResponse(
            String access_token,
            String token_type
    ) {}

    private record GithubUserResponse(
            Long id,
            String login,
            String name,
            String email
    ) {}

    private record GithubEmailResponse(
            String email,
            boolean primary,
            boolean verified
    ) {}

    private record GithubTokenRequest(
            String client_id,
            String client_secret,
            String code
    ) {}

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.github;
    }

    @Override
    public ExternalUserProfile authenticate(String tokenString) {

        String accessToken = retrieveAccessToken(tokenString);
        GithubUserResponse userResponse = retrieveUserData(accessToken);

        String email = userResponse.email();
        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(accessToken);
        }
        String displayName = userResponse.name() != null ? userResponse.name() : userResponse.login();
        return ExternalUserProfile.builder()
                .userId(String.valueOf(userResponse.id()))
                .username(userResponse.login())
                .email(email)
                .name(displayName)
                .build();
    }

    private @NonNull GithubUserResponse retrieveUserData(String accessToken) {
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        userHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> userRequest = new HttpEntity<>(userHeaders);
        ResponseEntity<GithubUserResponse> userResponseEntity = restTemplate.exchange(USER_ENDPOINT, HttpMethod.GET, userRequest, GithubUserResponse.class);
        GithubUserResponse userResponse = userResponseEntity.getBody();
        if (userResponse == null) {
            throw new RuntimeException("Nie udało się pobrać profilu użytkownika z GitHuba");
        }
        return userResponse;
    }

    private @NonNull String retrieveAccessToken(String tokenString) {
        HttpHeaders tokenHeaders = new HttpHeaders();
        tokenHeaders.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        var tokenRequestBody = new GithubTokenRequest(clientId, clientSecret, tokenString);
        ResponseEntity<GithubTokenResponse> tokenResponseEntity = restTemplate.postForEntity(
                ACCESS_TOKEN_ENDPOINT,
                new HttpEntity<>(tokenRequestBody, tokenHeaders),
                GithubTokenResponse.class
        );
        GithubTokenResponse tokenResponse = tokenResponseEntity.getBody();
        if (tokenResponse == null || tokenResponse.access_token() == null) {
            throw new RuntimeException("Nie udało się pobrać access_token z GitHuba");
        }
        return tokenResponse.access_token();
    }

    private String fetchPrimaryEmail(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GithubEmailResponse[]> responseEntity = restTemplate.exchange(EMAILS_ENDPOINT, HttpMethod.GET, request, GithubEmailResponse[].class);

        GithubEmailResponse[] emails = responseEntity.getBody();

        if (emails != null) {
            for (GithubEmailResponse emailDto : emails) {
                if (emailDto.primary() && emailDto.verified()) {
                    return emailDto.email();
                }
            }
        }
        throw new RuntimeException("Użytkownik GitHuba nie posiada zweryfikowanego adresu e-mail.");
    }
}
