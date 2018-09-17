package com.me4502.cab432.aws;

import com.amazonaws.AmazonClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.DetectLabelsRequest;
import com.amazonaws.services.rekognition.model.Image;
import com.amazonaws.services.rekognition.model.Label;
import com.me4502.cab432.app.PhotoApp;
import spark.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * The connector with the AWS service.
 */
public class AwsConnector {

    private final PhotoApp app;

    private final AmazonRekognition rekognitionClient;

    public AwsConnector(PhotoApp app, String appKey, String secretKey) {
        this.app = app;

        AWSCredentials credentials;
        try {
            credentials = new BasicAWSCredentials(appKey, secretKey);
        } catch (Exception e) {
            throw new AmazonClientException("Cannot find credentials.", e);
        }

        this.rekognitionClient = AmazonRekognitionClientBuilder
                .standard()
                .withRegion(Regions.AP_SOUTHEAST_2)
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }

    /**
     * Get a list of scene labels for a given image.
     *
     * @param url The image URL
     * @return The labels
     */
    public List<Label> getLabelsForImage(URL url) {
        var labels = new ArrayList<Label>();

        try(var stream = new BufferedInputStream(url.openStream())) {
            var image = new Image().withBytes(ByteBuffer.wrap(IOUtils.toByteArray(stream)));
            var result = this.rekognitionClient.detectLabels(new DetectLabelsRequest().withImage(image));
            labels.addAll(result.getLabels());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return labels;
    }
}
