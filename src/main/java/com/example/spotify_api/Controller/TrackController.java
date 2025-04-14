package com.example.spotify_api.Controller;

import com.example.spotify_api.Entity.TrackMetadata;
import com.example.spotify_api.Service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/track")
public class TrackController {

    private final TrackService trackService;

    @Autowired
    public TrackController(TrackService trackService) {
        this.trackService = trackService;
    }

    @PostMapping("/metadata")
    public Mono<TrackMetadata> createTrackMetadata(@RequestParam String isrc) {
        return trackService.createTrackMetadata(isrc);
    }

    @GetMapping("/metadata")
    public Mono<TrackMetadata> getTrackMetadata(@RequestParam String isrc) {
        return trackService.getTrackMetadata(isrc);
    }

    @GetMapping(value = "/cover", produces = MediaType.IMAGE_JPEG_VALUE)
    public Mono<byte[]> getCoverImage(@RequestParam String isrc) {
        return trackService.getCoverImage(isrc);
    }

    @GetMapping("/")
    public String root() {
        return "Welcome to the Code Challenge API!";
    }
}
