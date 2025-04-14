package com.example.spotify_api.Repository;

import com.example.spotify_api.Entity.TrackMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface TrackMetadataRepository extends JpaRepository<TrackMetadata, Long> {
    Optional<TrackMetadata> findByIsrc(String isrc);
}
