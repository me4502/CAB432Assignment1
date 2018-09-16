package com.me4502.cab432.musixmatch;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import com.me4502.cab432.app.PhotoApp;
import org.jmusixmatch.Helper;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.config.Constants;
import org.jmusixmatch.config.Methods;
import org.jmusixmatch.entity.track.Track;
import org.jmusixmatch.entity.track.search.TrackSeachMessage;
import org.jmusixmatch.http.MusixMatchRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The connector with the Musixmatch service.
 */
public class MusixmatchConnector {

    private final PhotoApp app;

    private MusixMatch musixMatch;
    private String appKey;

    public MusixmatchConnector(PhotoApp app, String appKey) {
        this.app = app;

        this.appKey = appKey;
        this.musixMatch = new MusixMatch(appKey);
    }

    private List<Track> searchTracksBetter(String query) throws MusixMatchException {
        List<Track> trackList;
        TrackSeachMessage message;
        Map<String, Object> params = new HashMap<>();

        params.put(Constants.API_KEY, this.appKey);
        params.put(Constants.QUERY, query);
        params.put(Constants.PAGE, 0);
        params.put(Constants.PAGE_SIZE, 10);
        params.put(Constants.F_HAS_LYRICS, "1");
        params.put("s_track_rating", "desc");

        String response;

        response = MusixMatchRequest.sendRequest(Helper.getURLString(
                Methods.TRACK_SEARCH, params));

        Gson gson = new Gson();

        try {
            message = gson.fromJson(response, TrackSeachMessage.class);
        } catch (JsonParseException jpe) {
            throw new MusixMatchException(jpe.getMessage());
        }

        int statusCode = message.getTrackMessage().getHeader().getStatusCode();

        if (statusCode > 200) {
            throw new MusixMatchException("Status Code is not 200");
        }

        trackList = message.getTrackMessage().getBody().getTrack_list();

        return trackList;
    }

    public List<Track> getTrackByTerm(String term) {
        try {
            return searchTracksBetter(term);
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
     */
    public Map<String, String> getLyricsFromTrack(Track track) {
        if (track == null || track.getTrack().getHasLyrics() == 0) {
            return Map.of("lyrics", "Failed to lookup lyrics", "id", "-1");
        }
        String lyrics;
        try {
            lyrics = this.musixMatch.getLyrics(track.getTrack().getTrackId()).getLyricsBody();
        } catch (MusixMatchException e) {
            e.printStackTrace();
            return Map.of("lyrics", "Failed to lookup lyrics", "id", "-1");
        }
        if (lyrics.isEmpty()) {
            return Map.of("lyrics", "No lyrics found", "id", "-1");
        }
        lyrics = lyrics.substring(0, lyrics.indexOf("*****"));
        return Map.of("lyrics", lyrics, "id", String.valueOf(track.getTrack().getTrackId()));
    }
}
