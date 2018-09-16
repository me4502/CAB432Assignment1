package com.me4502.cab432.musixmatch;

import com.google.common.collect.Lists;
import com.me4502.cab432.app.PhotoApp;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.track.Track;

import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    public List<Track> getTrackByTerm(String term) {
        try {
            return this.musixMatch.searchTracks(term, "", "", 1, 5, true);
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return Lists.newArrayList();
        }
    }

    /**
     * Gets a track from a song and artist.
     *
     * @param song The song
     * @param artist The artist
     * @return The track, if present
     */
    public Optional<Track> getTrackForSong(String song, String artist) {
        try {
            return Optional.of(this.musixMatch.getMatchingTrack(song, artist));
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Gets a track by ID
     *
     * @param id The ID
     * @return The track, if present
     */
    public Optional<Track> getTrackById(int id) {
        try {
            return Optional.of(this.musixMatch.getTrack(id));
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    /**
     * Gets the lyrics for a given track.
     *
     * @param track The track
     * @return The lyrics
     * @throws MusixMatchException If the API lookup failed
     */
    public Map<String, String> getLyricsFromTrack(Track track) {
        if (track == null || track.getTrack().getHasLyrics() == 0) {
            return Map.of("lyrics", "Failed to lookup lyrics", "id", "-1");
        }
        String lyrics = null;
        try {
            lyrics = this.musixMatch.getLyrics(track.getTrack().getTrackId()).getLyricsBody();
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return Map.of("lyrics", "Failed to lookup lyrics", "id", "-1");
        }
        lyrics = lyrics.substring(0, lyrics.indexOf("*****"));
        return Map.of("lyrics", lyrics, "id", String.valueOf(track.getTrack().getTrackId()));
    }
}
