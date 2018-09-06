package com.me4502.cab432.musixmatch;

import com.me4502.cab432.app.PhotoApp;
import org.jmusixmatch.MusixMatch;

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
}
