package com.me4502.cab432.app;

import static freemarker.template.Configuration.VERSION_2_3_26;
import static spark.Spark.get;
import static spark.Spark.port;
import static spark.Spark.staticFiles;

import com.amazonaws.services.rekognition.model.Label;
import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.me4502.cab432.aws.AwsConnector;
import com.me4502.cab432.flickr.FlickrConnector;
import com.me4502.cab432.lastfm.LastFmConnector;
import com.me4502.cab432.musixmatch.MusixmatchConnector;
import freemarker.template.Configuration;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import spark.ModelAndView;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The base app class for this application
 */
public class PhotoApp {

    public static final boolean DEBUG = System.getProperty("debug", "false").equals("true");

    // This is a Singleton class - setup as soon as it's first referenced.
    private static final PhotoApp instance = new PhotoApp();

    // Connectors
    private FlickrConnector flickrConnector;
    private MusixmatchConnector musixmatchConnector;
    private LastFmConnector lastFmConnector;
    private AwsConnector awsConnector;

    // Gson
    private Gson gson = new GsonBuilder().create();

    private Map<String, String> tagMapping = new HashMap<>();
    private Multimap<String, String> labelMapping = MultimapBuilder.hashKeys().hashSetValues().build();

    private PhotoApp() {
    }

    /**
     * Loads the main app content.
     */
    public void load() throws IOException {
        loadTagMapping();
        loadConfigurationAndConnectors();

        loadWebServer();
    }

    /**
     * Load the configuration file and setup the API connectors.
     */
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

            var awsApi = root.getNode("aws_access_key").getString("ENTER_API_KEY");
            var awsSecret = root.getNode("aws_secret_key").getString("ENTER_API_SECRET");

            configManager.save(root);

            // Try to use those keys to load the connectors.
            this.flickrConnector = new FlickrConnector(this, flickrSecret, flickrApi);
            this.musixmatchConnector = new MusixmatchConnector(this, musixmatchApi);
            this.lastFmConnector = new LastFmConnector(this, lastFmApi, lastFmSecret);
            this.awsConnector = new AwsConnector(this, awsApi, awsSecret);
        } catch (Exception e) {
            // If an exception occurs here, it's bad - runtime it.
            throw new RuntimeException(e);
        }
    }

    /**
     * Helper function to respond with a bad request.
     *
     * @param response The response object
     * @param message The error message
     * @return The json-ified error message
     */
    private String badRequest(Response response, String message) {
        response.status(400);
        response.header("Bad Request", message);
        return gson.toJson(Map.of("error", message));
    }

    /**
     * Setup the webserver configuration and routes.
     */
    private void loadWebServer() {
        port(Integer.parseInt(System.getProperty("photo_app.port", "5078")));
        if (DEBUG) {
            // During debug this allows hot-reloading the static changes.
            staticFiles.externalLocation("src/main/resources/static");
        } else {
            staticFiles.location("/static");
        }

        // Setup routes
        get("/image/search/:term", (request, response)
                -> gson.toJson(getFlickrConnector().getUrlsForSearch(request.params("term"))));
        get("/image/search_single/:term", (request, response)
                -> getFlickrConnector().getUrlForSearch(request.params("term"))
                .map(url -> gson.toJson(Map.of("url", url)))
                .orElseGet(() -> badRequest(response, "Failed to find image")));
        get("/image/tag/:image", (request, response) -> {
            var url = getFlickrConnector().getUrlForId(request.params("image"));
            if (url.isPresent()) {
                var labels = getAwsConnector().getLabelsForImage(url.get()).stream()
                        .sorted(Comparator.comparingDouble(Label::getConfidence).reversed())
                        .map(Label::getName)
                        .collect(Collectors.toList());
                if (DEBUG) {
                    // Debug code to assist in making tag mapping entries
                    System.out.println(labels.stream()
                            .map(String::toLowerCase)
                            .filter(label -> !tagMapping.keySet().contains(label))
                            .collect(Collectors.joining(", ")));
                }
                return gson.toJson(getGenreTagsForLabels(labels));
            } else {
                return badRequest(response, "Invalid Image");
            }
        });
        get("/track/get/:tags", (request, response)
                -> getLastFmConnector().getSingleSongByTags(Arrays.asList(request.params("tags").split(",")))
                        .map(track -> gson.toJson(Map.of("artist", track.getArtist(), "track", track.getName())))
                        .orElseGet(() -> badRequest(response, "Failed to find a track by tags!")));
        get("/track/search/:term", (request, response)
                -> gson.toJson(getMusixmatchConnector().getTrackByTerm(request.params("term")).stream()
                .map(track -> Map.of(
                        "id", track.getTrack().getTrackId(),
                        "song", track.getTrack().getTrackName(),
                        "artist", track.getTrack().getArtistName()))
                .collect(Collectors.toList())));
        get("/track/lyrics/:name/:artist", (request, response)
                -> getMusixmatchConnector().getTrackForSong(request.params("name"), request.params("artist"))
                                .map(track -> gson.toJson(getMusixmatchConnector().getLyricsFromTrack(track)))
                                .orElseGet(() -> badRequest(response, "Failed to find lyrics")));
        get("/track/lyrics/:trackId", (request, response)
                -> getMusixmatchConnector().getTrackById(Integer.parseInt(request.params("trackId")))
                .map(track -> gson.toJson(getMusixmatchConnector().getLyricsFromTrack(track)))
                .orElseGet(() -> badRequest(response, "Failed to find lyrics")));
        get("/tag/popular", (request, response)
                -> gson.toJson(getLastFmConnector().getPopularTags()));
        get("/label/get/:song/:artist", (request, response)
                -> gson.toJson(getLabelsForGenreTags(getLastFmConnector().getTagsFromSong(request.params("song"), request.params("artist")))));

        // Web Routes
        // Standard Flow
        get("/", (request, response)
                -> render(Map.of("title", "Poster Creator"), "index.ftl"));
        get("/select_tags", (request, response)
                -> render(Map.of("title", "Select Tags"), "select_tags.ftl"));
        get("/export", (request, response)
                -> render(Map.of("title", "Export"), "export.ftl"));

        // Song Flow
        get("/by_song", (request, response)
                -> render(Map.of("title", "Create by Song"), "find_song.ftl"));
        get("/select_labels", (request, response)
                -> render(Map.of("title", "Select Labels"), "select_labels.ftl"));
    }

    /**
     * Helper method to render Freemarker template files.
     *
     * @param model The model
     * @param templatePath The template path
     * @return The rendered template
     */
    private static String render(Map<String, Object> model, String templatePath) {
        freemarker.template.Configuration config = new Configuration(VERSION_2_3_26);
        config.setClassForTemplateLoading(PhotoApp.class, "/templates/");

        return new FreeMarkerEngine(config).render(new ModelAndView(model, templatePath));
    }

    /**
     * Load the tag mappings from tag_mapping.properties
     */
    private void loadTagMapping() throws IOException {
        try (var reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/tag_mapping.properties")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isEmpty() || !line.contains("=")) {
                    throw new IOException("Invalid tag mapping file format!");
                }
                line = line.toLowerCase();
                String[] parts = line.split("=", 2);
                tagMapping.put(parts[0], parts[1]);
                labelMapping.put(parts[1], parts[0]);
            }
        }
    }

    /**
     * The mapping of Label -> Tag
     *
     * @return The tag mapping
     */
    public Map<String, String> getTagMapping() {
        return this.tagMapping;
    }

    /**
     * The mapping of Tag -> Labels
     *
     * @return The label mapping
     */
    public Multimap<String, String> getLabelMapping() {
        return this.labelMapping;
    }

    /**
     * Converts Rekognition scene labels into genre tags for Last.FM
     *
     * @param labels The labels from Rekognition
     * @return The genre tags for Last.FM
     */
    private List<String> getGenreTagsForLabels(List<String> labels) {
        return labels.stream()
                .map(String::toLowerCase)
                .filter(lowerLabel -> tagMapping.containsKey(lowerLabel))
                .map(lowerLabel -> tagMapping.get(lowerLabel))
                .distinct()
                .collect(Collectors.toList());
    }

    private List<String> getLabelsForGenreTags(List<String> tags) {
        return tags.stream()
                .map(String::toLowerCase)
                .filter(lowerTag -> labelMapping.containsKey(lowerTag))
                .map(lowerTag -> labelMapping.get(lowerTag))
                .flatMap(Collection::stream)
                .distinct()
                .limit(20)
                .collect(Collectors.toList());
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
