package net.patrykdobrowolski.auth.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
// TODO refactoring
public class GithubAuthenticationProvider implements OAuth2AuthenticationProvider {

    @Value("${oauth2.github.client-id}")
    private String clientId;

    @Value("${oauth2.github.client-secret}")
    private String clientSecret;

    private final RestClient restClient = RestClient.create();

    private record GithubTokenResponse(
            @JsonProperty("access_token") String accessToken,
            @JsonProperty("token_type") String tokenType
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

    @Override
    public boolean supports(AuthProvider authProvider) {
        return authProvider == AuthProvider.GITHUB;
    }

    @Override
    public ExternalUserProfile authenticate(String tokenString) {
        GithubTokenResponse tokenResponse = restClient.post()
                .uri("https://github.com/login/oauth/access_token")
                .header("Accept", "application/json")
                .body(Map.of(
                        "client_id", clientId,
                        "client_secret", clientSecret,
                        "code", tokenString
                ))
                .retrieve()
                .body(GithubTokenResponse.class);

        if (tokenResponse == null || tokenResponse.accessToken() == null) {
            throw new RuntimeException("Nie udało się pobrać access_token z GitHuba");
        }
        String accessToken = tokenResponse.accessToken();
        GithubUserResponse userResponse = restClient.get()
                .uri("https://api.github.com/user")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .body(GithubUserResponse.class);

        if (userResponse == null) {
            throw new RuntimeException("Nie udało się pobrać profilu użytkownika z GitHuba");
        }

        String email = userResponse.email();
        if (email == null || email.isBlank()) {
            email = fetchPrimaryEmail(accessToken);
        }

        String displayName = userResponse.name() != null ? userResponse.name() : userResponse.login();

        return new ExternalUserProfile(
                String.valueOf(userResponse.id()),
                "github:" + userResponse.login(),
                userResponse.email(),
                displayName
        );
    }

    private String fetchPrimaryEmail(String accessToken) {
        GithubEmailResponse[] emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .retrieve()
                .body(GithubEmailResponse[].class);

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
