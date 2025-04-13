package com.example.spotify_api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/track")
public class TrackController {

    private final SpotifyAuthService authService;

    @Autowired
    public TrackController(SpotifyAuthService authService) {
        this.authService = authService;
    }

//    @GetMapping("/metadata")
//    public Mono<TrackMetadata> getTrackMetadata(@RequestParam String isrc) {
//        return authService.getAccessToken()
//                .flatMap(token -> WebClient.builder()
//                        .baseUrl("https://api.spotify.com/v1")
//                        .defaultHeader("Authorization", "Bearer " + token)
//                        .build()
//                        .get()
//                        .uri(uriBuilder -> uriBuilder
//                                .path("/search")
//                                .queryParam("q", "isrc:" + isrc)
//                                .queryParam("type", "track")
//                                .queryParam("limit", "1")
//                                .build())
//                        .retrieve()
//                        .bodyToMono(SpotifyTrackSearchResponse.class));
////                ).map(response -> {
////                    if (response.tracks().items().isEmpty()) {
////                        throw new RuntimeException("No track found for ISRC: " + isrc);
////                    }
////                    SpotifyTrack item = response.tracks().items().get(0);
////                    return new TrackMetadata(
////                            item.name(),
////                            item.artists().get(0).name(),
////                            item.album().name(),
////                            item.album().id(),
////                            item.explicit(),
////                            item.duration_ms() / 1000
////                    );
////                });
//    }

    @GetMapping("/metadata")
    public Mono<String> getTrackMetadata(@RequestParam String isrc) {
        return authService.getAccessToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl("https://api.spotify.com/v1")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build()
                        .get()
                        .uri(uriBuilder -> uriBuilder
                                .path("/search")
                                .queryParam("q", "isrc:" + isrc)
                                .queryParam("type", "track")
                                .queryParam("limit", "1")
                                .build())
                        .retrieve()
                        .bodyToMono(String.class)
                );
    }

    @GetMapping("/")
    public String root() {
        return "Welcome to the Code Challenge API!";
    }
}
