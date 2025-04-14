package com.example.spotify_api.Controllers;

import com.example.spotify_api.Repositories.TrackMetadataRepository;
import com.example.spotify_api.Services.SpotifyAuthService;
import com.example.spotify_api.Entities.TrackMetadata;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@RestController
@RequestMapping("/track")
public class TrackController {

    private final SpotifyAuthService authService;

    @Autowired
    public TrackController(SpotifyAuthService authService) {
        this.authService = authService;
    }
    @Autowired
    private TrackMetadataRepository repository;

    @PostMapping("/metadata")
    public Mono<TrackMetadata> trackMetadata(@RequestParam String isrc) {
        Optional<TrackMetadata> existing = repository.findByIsrc(isrc);
        if (existing.isPresent()) {
            return Mono.error(new IllegalArgumentException("Track with ISRC already exists"));
        }

        return getTrackMetadataFromSpotify(isrc)
                .flatMap(json -> {
                    try {
                        ObjectMapper mapper = new ObjectMapper();
                        JsonNode root = mapper.readTree(json);
                        JsonNode trackNode = root.path("tracks").path("items").get(0);

                        TrackMetadata metadata = new TrackMetadata();
                        metadata.setIsrc(isrc);
                        metadata.setName(trackNode.path("name").asText());
                        metadata.setArtistName(trackNode.path("artists").get(0).path("name").asText());
                        metadata.setAlbumName(trackNode.path("album").path("name").asText());
                        metadata.setAlbumId(trackNode.path("album").path("id").asText());
                        metadata.setExplicit(trackNode.path("explicit").asBoolean());
                        metadata.setPlaybackSeconds(trackNode.path("duration_ms").asInt() / 1000);

                        String albumId = metadata.getAlbumId();

                        return getAlbumInfo(albumId)
                                .flatMap(albumJson -> {
                                    try {
                                        JsonNode albumRoot = mapper.readTree(albumJson);
                                        String imageUrl = albumRoot.path("images").get(0).path("url").asText();

                                        return downloadImageReactive(imageUrl)
                                                .map(imageBytes -> {
                                                    metadata.setCoverImage(imageBytes);
                                                    repository.save(metadata);
                                                    return metadata;
                                                });
                                    } catch (Exception e) {
                                        return Mono.error(new RuntimeException("Error parsing album image", e));
                                    }
                                });
                    } catch (Exception e) {
                        return Mono.error(new RuntimeException("Error parsing Spotify response", e));
                    }
                });
    }

    @GetMapping("/metadata")
    public Mono<TrackMetadata> getTrackMetadata(String isrc){
        Optional<TrackMetadata> optionalTrack = repository.findByIsrc(isrc);
        if (optionalTrack.isPresent()) {
            return Mono.just(optionalTrack.get());
        } else {
            return Mono.error(new IllegalArgumentException("Track with ISRC " + isrc + " not found."));
        }
    }

    @GetMapping(value = "/cover", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> getCoverImage(@RequestParam String isrc) {
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


    private Mono<String> getTrackMetadataFromSpotify(String isrc) {
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

    private Mono<String> getAlbumInfo(String albumId) {
        return authService.getAccessToken()
                .flatMap(token -> WebClient.builder()
                        .baseUrl("https://api.spotify.com/v1")
                        .defaultHeader("Authorization", "Bearer " + token)
                        .build()
                        .get()
                        .uri("/albums/" + albumId)
                        .retrieve()
                        .bodyToMono(String.class));
    }

    private Mono<byte[]> downloadImageReactive(String url) {
        return WebClient.create()
                .get()
                .uri(url)
                .retrieve()
                .bodyToMono(byte[].class);
    }

//    @GetMapping("/getTrackMetadata")
//    public ResponseEntity<TrackMetadata> getStoredMetadata(@RequestParam String isrc){
//        Optional<TrackMetadata> metadata = metadataService.getMetadataByIsrc
//    }

    @GetMapping("/")
    public String root() {
        return "Welcome to the Code Challenge API!";
    }
}
