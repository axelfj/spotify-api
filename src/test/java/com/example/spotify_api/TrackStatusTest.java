package com.example.spotify_api;
import com.example.spotify_api.Service.TrackService;
import com.example.spotify_api.Service.SpotifyAuthService;
import com.example.spotify_api.Repository.TrackMetadataRepository;
import com.example.spotify_api.Entity.TrackMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import java.util.Optional;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
class TrackStatusTest {
 @Test void missingTrackAndCoverAre404() {
  var repo=mock(TrackMetadataRepository.class);
  when(repo.findByIsrc("missing")).thenReturn(Optional.empty());
  var service=new TrackService(mock(SpotifyAuthService.class),repo);
  assertEquals(404,assertThrows(ResponseStatusException.class,()->service.getTrackMetadata("missing").block()).getStatusCode().value());
  assertEquals(404,assertThrows(ResponseStatusException.class,()->service.getCoverImage("missing").block()).getStatusCode().value());
 }
 @Test void duplicateIs409AndDoesNotContactSpotify() {
  var repo=mock(TrackMetadataRepository.class);var auth=mock(SpotifyAuthService.class);
  when(repo.findByIsrc("existing")).thenReturn(Optional.of(new TrackMetadata()));
  var service=new TrackService(auth,repo);
  assertEquals(409,assertThrows(ResponseStatusException.class,()->service.createTrackMetadata("existing").block()).getStatusCode().value());
  assertEquals(404,assertThrows(ResponseStatusException.class,()->service.getCoverImage("existing").block()).getStatusCode().value());
  verifyNoInteractions(auth);
 }
}
