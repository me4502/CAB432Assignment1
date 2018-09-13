package com.me4502.cab432.lastfm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.me4502.cab432.app.PhotoApp;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * The connector for the Last.FM service.
 */
public class LastFmConnector {

    private final static boolean PRELOAD_CACHE = false;

    private final PhotoApp app;

    private String appKey;
    private String appSecret;

    private Cache<String, Track> trackCache = CacheBuilder.newBuilder()
            .build();

    public LastFmConnector(PhotoApp app, String appKey, String appSecret) {
        this.app = app;

        this.appKey = appKey;
        this.appSecret = appSecret;

        Caller.getInstance().getLogger().setLevel(Level.WARNING);

        if (PRELOAD_CACHE) {
            Thread cacherThread = new Thread(this::populateCaches);
            cacherThread.setDaemon(true);
            cacherThread.setName("Last.FM Cache Populator Thread");
            cacherThread.start();
        }
    }

    /**
     * Populate the trackCache using the default tags.
     */
    private void populateCaches() {
        app.getTagMapping().values().stream().distinct().forEach(this::getTracksFromTag);
    }

    public List<String> getPopularTags() {
        return Tag.getTopTags(appKey).stream()
                .map(Tag::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    private synchronized Track getFullTrack(Track track) {
        if (!track.getTags().isEmpty()) {
            return track;
        }
        return Track.getInfo(track.getArtist(), track.getName(), appKey);
    }

    public List<Track> getTracksFromTag(String tag) {
        return Tag.getTopTracks(tag, appKey)
                .stream()
                .map(track -> {
                    try {
                        return trackCache.get(track.getMbid(), () -> getFullTrack(track));
                    } catch (ExecutionException e) {
                        e.printStackTrace();
                    }
                    return track;
                })
                .filter(track -> !track.getTags().isEmpty())
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
