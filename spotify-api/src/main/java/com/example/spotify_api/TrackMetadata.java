package com.example.spotify_api;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TrackMetadata {

    private String name;
    private String artistName;
    private String albumName;
    private String albumId;
    private boolean isExplicit;
    private long playbackSeconds;


}
