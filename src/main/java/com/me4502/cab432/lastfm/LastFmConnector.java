package com.me4502.cab432.lastfm;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.me4502.cab432.app.PhotoApp;
import de.umass.lastfm.Caller;
import de.umass.lastfm.Tag;
import de.umass.lastfm.Track;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
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
            // As these operations are expensive, pre-loading the cache is helpful.
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

    /**
     * Get a list of popular tags
     *
     * @return The popular tags
     */
    public List<String> getPopularTags() {
        return Tag.getTopTags(appKey).stream()
                .map(Tag::getName)
                .map(String::toLowerCase)
                .collect(Collectors.toList());
    }

    /**
     * Grab a full copy of a track from a basic one.
     *
     * @param track The basic track
     * @return The full track
     */
    private synchronized Track getFullTrack(Track track) {
        if (!track.getTags().isEmpty()) {
            return track;
        }
        return Track.getInfo(track.getArtist(), track.getName(), appKey);
    }

    /**
     * Get a list of top tracks for a given tag.
     *
     * @param tag The tag
     * @return The top tracks
     */
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

    /**
     * Get a single song that best matches the list of tags
     *
     * @param tags The tags
     * @return The song, if found
     */
    public Optional<Track> getSingleSongByTags(List<String> tags) {
        if (tags.isEmpty()) {
            return Optional.empty();
        }

        var tracks = new ArrayList<Track>();
        tags.stream().limit(3).map(this::getTracksFromTag).forEach(tracks::addAll);
        if (tracks.isEmpty()) {
            return Optional.empty();
        } else if (tracks.size() == 1 || tags.size() == 1) {
            return Optional.of(tracks.get(0));
        }

        return tracks.stream().max(Comparator.comparingInt(o -> scoreTags(o.getTags(), tags)));
    }

    /**
     * Helper method to create a "score" of how well the given tags match the wanted tags.
     *
     * @param currentTags The tags to test
     * @param wantedTags The wanted tags
     * @return The score
     */
    private int scoreTags(Collection<String> currentTags, List<String> wantedTags) {
        return currentTags.stream()
                .filter(wantedTags::contains)
                .mapToInt(wantedTags::indexOf)
                .map(i -> wantedTags.size() - i)
                .sum();
    }
}
