package com.me4502.cab432.lastfm;

import com.google.common.collect.Lists;
import com.me4502.cab432.app.PhotoApp;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public List<String> getPopularTags() {
        return Tag.getTopTags(appKey).stream()
                .map(Tag::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    public List<Track> getTracksFromTag(String tag) {
        return Tag.getTopTracks(tag, appKey)
                .stream()
                .map(track -> Track.getInfo(track.getArtist(), track.getMbid(), appKey))
                .filter(track -> !track.getTags().isEmpty())
                .limit(10)
                .collect(Collectors.toList());
    }

    public Optional<Track> getSingleSongByTags(List<String> tags) {
        if (tags.isEmpty()) {
            return Optional.empty();
        }
        var tracks = getTracksFromTag(tags.get(0));
        if (tracks.isEmpty()) {
            return Optional.empty();
        } else if (tracks.size() == 1 || tags.size() == 1) {
            return Optional.of(tracks.get(0));
        }

        return tracks.stream().max((o1, o2) -> {
            List<String> track1Tags = Lists.newArrayList(o1.getTags());
            List<String> track2Tags = Lists.newArrayList(o2.getTags());
            track1Tags.retainAll(tags);
            track2Tags.retainAll(tags);
            return Integer.compare(track1Tags.size(), track2Tags.size());
        });
    }
}
