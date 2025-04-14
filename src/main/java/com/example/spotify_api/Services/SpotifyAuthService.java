package com.example.spotify_api.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Base64;

@Service
public class SpotifyAuthService {
    @Value("${spotify.client-id}")
    private String clientId;

    @Value("${spotify.client-secret}")
    private String clientSecret;

    private final WebClient authClient = WebClient.builder()
            .baseUrl("https://accounts.spotify.com/api"
            ).build();

    public Mono<String> getAccessToken() {
        String credentials = clientId + ":" + clientSecret;
        String encodedCredentials = Base64.getEncoder().encodeToString(credentials.getBytes());

        return authClient.post()
                .uri("/token")
                .header("Authorization", "Basic " + encodedCredentials)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .bodyValue("grant_type=client_credentials")
                .retrieve()
                .bodyToMono(SpotifyAuthResponse.class)
                .map(SpotifyAuthResponse::access_token);
    }

    private record SpotifyAuthResponse(
            String access_token,
            String token_type,
            int expires_in
    ) {}

}
