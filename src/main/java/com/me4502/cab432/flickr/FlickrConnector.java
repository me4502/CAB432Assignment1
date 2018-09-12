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
        SearchParameters parameters = new SearchParameters();
        parameters.setText(search);
        List<Photo> photoList = flickr.getPhotosInterface().search(parameters, 5, 1);
        var urls = new ArrayList<Map<String, String>>();
        for (Photo photo : photoList) {
            var data = new HashMap<String, String>();
            data.put("url", photo.getMediumUrl());
            data.put("id", photo.getId());
            urls.add(data);
        }
        return urls;
    }

    public URL getUrlForId(String id) throws FlickrException, MalformedURLException {
        return new URL(flickr.getPhotosInterface().getPhoto(id).getMediumUrl());
    }
}
