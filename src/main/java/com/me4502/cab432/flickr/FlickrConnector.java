package com.me4502.cab432.flickr;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.photos.Photo;
import com.flickr4java.flickr.photos.SearchParameters;
import com.flickr4java.flickr.test.TestInterface;
import com.google.common.collect.Maps;
import com.me4502.cab432.app.PhotoApp;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The connector with the Flickr services
 */
public class FlickrConnector {

    private final PhotoApp app;

    private Flickr flickr;

    public FlickrConnector(PhotoApp app, String appSecret, String appKey) {
        this.app = app;

        try {
            this.flickr = new Flickr(appKey, appSecret, new REST());

            TestInterface testInterface = this.flickr.getTestInterface();
            testInterface.echo(Maps.newHashMap());
        } catch (FlickrException e) {
            throw new RuntimeException(e); // Re-throw as a runtime exception
        }
    }

    private SearchParameters createParameters(String text, boolean tagMode) {
        SearchParameters parameters = new SearchParameters();
        if (tagMode) {
            parameters.setTagMode("any");
            var tagsSplit = text.split(",");
            String[] tags;
            if (tagsSplit.length > 20) {
                tags = new String[20];
                System.arraycopy(tagsSplit, 0, tags, 0, 20);
            } else {
                tags = tagsSplit;
            }
            parameters.setTags(tags);
        } else {
            parameters.setText(text);
        }
        parameters.setSort(SearchParameters.RELEVANCE);
        parameters.setSafeSearch(Flickr.SAFETYLEVEL_MODERATE);
        try {
            parameters.setMedia("photos");
        } catch (FlickrException e) {
            throw new RuntimeException(e);
        }
        return parameters;
    }

    /**
     * Grabs a single URL for the search term.
     *
     * @param search The search term.
     * @return The single URL, if present
     * @throws FlickrException If the Flickr API throws an error
     */
    public Optional<String> getUrlForSearch(String search) throws FlickrException {
        return flickr.getPhotosInterface().search(createParameters(search, true), 1, 1).stream()
                .map(Photo::getMediumUrl)
                .findFirst()
                .or(() -> {
                    try {
                        return flickr.getPhotosInterface()
                                .search(createParameters(search.split(",")[0], false), 1, 1).stream()
                                .map(Photo::getMediumUrl)
                                .findFirst();
                    } catch (FlickrException e) {
                        e.printStackTrace();
                        return Optional.empty();
                    }
                });
    }

    /**
     * Grabs a list of image URLs for the search term.
     *
     * This will have a limit of 5 URLs.
     *
     * @param search The search term.
     * @return The list of up to 5 URLs
     * @throws FlickrException If the Flickr API throws an error
     */
    public List<Map<String, String>> getUrlsForSearch(String search) throws FlickrException {
        List<Photo> photoList = flickr.getPhotosInterface().search(createParameters(search, false), 5, 1);
        var urls = new ArrayList<Map<String, String>>();
        for (Photo photo : photoList) {
            var data = new HashMap<String, String>();
            data.put("url", photo.getMediumUrl());
            data.put("id", photo.getId());
            urls.add(data);
        }
        return urls;
    }

    /**
     * Gets a photo URL for a photo ID.
     *
     * @param id The ID
     * @return The photo URL
     * @throws FlickrException If the lookup failed.
     */
    public Optional<URL> getUrlForId(String id) throws FlickrException {
        try {
            return Optional.of(new URL(flickr.getPhotosInterface().getPhoto(id).getMediumUrl()));
        } catch (MalformedURLException e) {
            return Optional.empty();
        }
    }
}
