package com.me4502.cab432.flickr;

import com.aetrion.flickr.Flickr;
import com.aetrion.flickr.FlickrException;
import com.aetrion.flickr.REST;
import com.aetrion.flickr.photos.Photo;
import com.aetrion.flickr.photos.SearchParameters;
import com.me4502.cab432.app.PhotoApp;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

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
        } catch (ParserConfigurationException e) {
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
     * @throws SAXException If a SAX error occurs
     * @throws IOException If an IO error occurs
     * @throws FlickrException If the Flickr API throws an error
     */
    @SuppressWarnings("unchecked")
    public List<String> getUrlsForSearch(String search) throws SAXException, IOException, FlickrException {
        SearchParameters parameters = new SearchParameters();
        parameters.setText(search);
        List<Photo> photoList = (List<Photo>) flickr.getPhotosInterface().search(parameters, 5, 1);
        return photoList.stream().map(Photo::getUrl).collect(Collectors.toList());
    }
}
