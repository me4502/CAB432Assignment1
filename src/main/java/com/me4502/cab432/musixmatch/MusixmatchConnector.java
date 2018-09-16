package com.me4502.cab432.musixmatch;

import com.me4502.cab432.app.PhotoApp;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.track.Track;

import java.util.Map;

/**
 * The connector with the Musixmatch service.
 */
public class MusixmatchConnector {

    private final PhotoApp app;

    private MusixMatch musixMatch;

    public MusixmatchConnector(PhotoApp app, String appKey) {
        this.app = app;

        this.musixMatch = new MusixMatch(appKey);
    }

    /**
     * Gets a track from a song and artist.
     *
     * @param song The song
     * @param artist The artist
     * @return The track
     * @throws MusixMatchException If the lookup failed
     */
    public Track getTrackForSong(String song, String artist) throws MusixMatchException {
        return this.musixMatch.getMatchingTrack(song, artist);
    }

    /**
     * Gets a track by ID
     *
     * @param id The ID
     * @return The track
     * @throws MusixMatchException If the lookup failed
     */
    public Track getTrackById(int id) throws MusixMatchException {
        return this.musixMatch.getTrack(id);
    }

    /**
     * Gets the lyrics for a given track.
     *
     * @param track The track
     * @return The lyrics
     * @throws MusixMatchException If the API lookup failed
     */
    public Map<String, String> getLyricsFromTrack(Track track) throws MusixMatchException {
        if (track == null || track.getTrack().getHasLyrics() == 0) {
            return Map.of("lyrics", "Failed to lookup lyrics", "id", "-1");
        }
        String lyrics = this.musixMatch.getLyrics(track.getTrack().getTrackId()).getLyricsBody();
        lyrics = lyrics.substring(0, lyrics.indexOf("*****"));
        return Map.of("lyrics", lyrics, "id", String.valueOf(track.getTrack().getTrackId()));
    }
}
