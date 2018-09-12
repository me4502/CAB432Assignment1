package com.me4502.cab432.musixmatch;

import com.me4502.cab432.app.PhotoApp;
import org.jmusixmatch.MusixMatch;
import org.jmusixmatch.MusixMatchException;
import org.jmusixmatch.entity.lyrics.Lyrics;
import org.jmusixmatch.entity.track.Track;

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

    public Track getTrackForSong(String song, String artist) throws MusixMatchException {
        return this.musixMatch.getMatchingTrack(song, artist);
    }

    public Optional<Lyrics> getLyricsFromTrack(Track track) throws MusixMatchException {
        if (track.getTrack().getHasLyrics() == 0) {
            return Optional.empty();
        }
        return Optional.of(this.musixMatch.getLyrics(track.getTrack().getTrackId()));
    }
}
