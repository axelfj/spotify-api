package com.example.spotify_api.Repositories;

import com.example.spotify_api.Entities.TrackMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrackMetadataRepository extends JpaRepository<TrackMetadata, Long> {
    // Método para encontrar TrackMetadata por ISRC
    Optional<TrackMetadata> findByIsrc(String isrc);
}
