package com.me4502.cab432.app;

import com.me4502.cab432.aws.AwsConnector;
import com.me4502.cab432.flickr.FlickrConnector;
import com.me4502.cab432.lastfm.LastFmConnector;
import com.me4502.cab432.musixmatch.MusixmatchConnector;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;

import java.io.IOException;
import java.nio.file.Paths;

import static spark.Spark.*;

/**
 * The base app class for this application
 */
public class PhotoApp {

    private static PhotoApp instance;

    // Connectors
    private FlickrConnector flickrConnector;
    private MusixmatchConnector musixmatchConnector;
    private LastFmConnector lastFmConnector;
    private AwsConnector awsConnector;

    public PhotoApp() {
        PhotoApp.instance = this;
    }

    /**
     * Loads the main app content.
     */
    public void load() {
        loadConfigurationAndConnectors();

        loadWebServer();
    }

    private void loadConfigurationAndConnectors() {
        ConfigurationLoader<CommentedConfigurationNode> configManager = HoconConfigurationLoader.builder()
                .setPath(Paths.get("photo_app.conf"))
                .setDefaultOptions(ConfigurationOptions.defaults().setShouldCopyDefaults(true))
                .build();

        try {
            CommentedConfigurationNode root = configManager.load();

            var flickrApi = root.getNode("flickr_api").getString("ENTER_API_KEY");
            var flickrSecret = root.getNode("flickr_secret").getString("ENTER_API_SECRET");

            var musixmatchApi = root.getNode("musixmatch_api").getString("ENTER_API_KEY");

            var lastFmApi = root.getNode("last_fm_api").getString("ENTER_API_KEY");
            var lastFmSecret = root.getNode("last_fm_secret").getString("ENTER_API_SECRET");

            configManager.save(root);

            // Try to use those keys to load the connectors.
            this.flickrConnector = new FlickrConnector(this, flickrSecret, flickrApi);
            this.musixmatchConnector = new MusixmatchConnector(this, musixmatchApi);
            this.lastFmConnector = new LastFmConnector(this, lastFmApi, lastFmSecret);
            this.awsConnector = new AwsConnector(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadWebServer() {
        // Setup routes
        get("/image/search/:term", (request, response) -> "Searching for " + request.params("term"));
    }

    /**
     * Gets the Flickr connector.
     *
     * @return The flickr connector
     */
    public FlickrConnector getFlickrConnector() {
        return this.flickrConnector;
    }

    /**
     * Gets the Musixmatch connector.
     *
     * @return The musixmatch connector
     */
    public MusixmatchConnector getMusixmatchConnector() {
        return this.musixmatchConnector;
    }

    /**
     * Gets the Last.FM connector.
     *
     * @return The last.fm connector
     */
    public LastFmConnector getLastFmConnector() {
        return this.lastFmConnector;
    }

    /**
     * Gets the AWS connector
     *
     * @return The AWS connector
     */
    public AwsConnector getAwsConnector() {
        return this.awsConnector;
    }

    /**
     * Gets the Singleton instance of this class
     *
     * @return The instance
     */
    public static PhotoApp getInstance() {
        return PhotoApp.instance;
    }
}
