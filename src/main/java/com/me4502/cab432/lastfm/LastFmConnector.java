package com.me4502.cab432.lastfm;

import com.me4502.cab432.app.PhotoApp;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;

import java.util.Collection;

/**
 * The connector for the Last.FM service.
 */
public class LastFmConnector {

    private final PhotoApp app;

    private String appKey;
    private String appSecret;

    public LastFmConnector(PhotoApp app, String appKey, String appSecret) {
        this.app = app;

        this.appKey = appKey;
        this.appSecret = appSecret;
    }

    public Collection<Track> getTracksFromTag(String tag) {
        return Tag.getTopTracks(tag, appKey);
    }
}
