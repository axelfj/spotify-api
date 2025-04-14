package com.example.spotify_api.Services;

import com.example.spotify_api.Entities.TrackMetadata;
import com.example.spotify_api.Repositories.TrackMetadataRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class TrackService {

    private final SpotifyAuthService authService;
    private final TrackMetadataRepository repository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Autowired
    public TrackService(SpotifyAuthService authService, TrackMetadataRepository repository) {
        this.authService = authService;
        this.repository = repository;
    }

    public Mono<TrackMetadata> createTrackMetadata(String isrc) {
        if (repository.findByIsrc(isrc).isPresent()) {
            return Mono.error(new IllegalArgumentException("Track with ISRC already exists"));
        }

        return getTrackFromSpotify(isrc)
                .flatMap(trackNode -> {
                    TrackMetadata metadata = parseTrackMetadata(trackNode, isrc);
                    return getAlbumImage(metadata.getAlbumId())
                            .flatMap(imageBytes -> {
                                metadata.setCoverImage(imageBytes);
                                repository.save(metadata);
                                return Mono.just(metadata);
                            });
                });
    }

    public Mono<TrackMetadata> getTrackMetadata(String isrc) {
        return repository.findByIsrc(isrc)
                .map(Mono::just)
                .orElseGet(() -> Mono.error(new IllegalArgumentException("Track with ISRC " + isrc + " not found.")));
    }

    public Mono<byte[]> getCoverImage(String isrc) {
        Optional<TrackMetadata> optionalTrack = repository.findByIsrc(isrc);

        if (optionalTrack.isPresent()) {
            byte[] image = optionalTrack.get().getCoverImage();
            if (image != null && image.length > 0) {
                return Mono.just(image);
            } else {
                return Mono.error(new IllegalArgumentException("Cover image not found for ISRC " + isrc));
            }
        } else {
            return Mono.error(new IllegalArgumentException("Track with ISRC " + isrc + " not found."));
        }
    }

    private Mono<JsonNode> getTrackFromSpotify(String isrc) {
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
                        .map(this::extractFirstTrackNode)
                );
    }

    private JsonNode extractFirstTrackNode(String json) {
        try {
            JsonNode root = mapper.readTree(json);
            return root.path("tracks").path("items").get(0);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing Spotify response", e);
        }
    }

    private TrackMetadata parseTrackMetadata(JsonNode node, String isrc) {
        TrackMetadata metadata = new TrackMetadata();
        metadata.setIsrc(isrc);
        metadata.setName(node.path("name").asText());
        metadata.setArtistName(node.path("artists").get(0).path("name").asText());
        metadata.setAlbumName(node.path("album").path("name").asText());
        metadata.setAlbumId(node.path("album").path("id").asText());
        metadata.setExplicit(node.path("explicit").asBoolean());
        metadata.setPlaybackSeconds(node.path("duration_ms").asInt() / 1000);
        return metadata;
    }

    private Mono<byte[]> getAlbumImage(String albumId) {
        return authService.getAccessToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl("https://api.spotify.com/v1")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build()
                        .get()
                        .uri("/albums/" + albumId)
                        .retrieve()
                        .bodyToMono(String.class))
                .map(this::extractImageUrl)
                .flatMap(this::downloadImage);
    }

    private String extractImageUrl(String albumJson) {
        try {
            JsonNode root = mapper.readTree(albumJson);
            return root.path("images").get(0).path("url").asText();
        } catch (Exception e) {
            throw new RuntimeException("Error parsing album image", e);
        }
    }

    private Mono<byte[]> downloadImage(String url) {
        return WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
