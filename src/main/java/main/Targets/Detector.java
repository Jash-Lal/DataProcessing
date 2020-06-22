package main.Targets;


import com.amazonaws.services.rekognition.model.BoundingBox;
import main.DataType.DataType;
import main.Elastic.ElasticClient;
import main.Metadata.DroneMetadata;
import main.Metadata.HandheldCameraMetadata;
import main.Metadata.Metadata;
import org.elasticsearch.action.index.IndexResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Uses AWS Rekognition to detect targets in an image.
 * Calculates information about targets that will be used in visualization.
 * Sends information to elasticsearch
 */
public class Detector {

    private ElasticClient elasticClient;
    private Metadata metadata;
    private TargetFinder targetFinder;
    private String s3Key;
    private String s3Bucket;

    /**
     * Creates an instance of Detector
     * @param s3Key is the file of the S3Object
     * @param s3Bucket is the bucket of the S3Object
     * @param dataType is the type of sensor the S3Object comes from
     * @param metadata is the object's metadata
     * @param elasticClient is the elastic client
     */
    public Detector(String s3Key, String s3Bucket, DataType dataType, Metadata metadata, ElasticClient elasticClient) {
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
        this.metadata = metadata;
        this.targetFinder = determineTargetFinder(s3Key, s3Bucket, dataType);
        this.elasticClient = elasticClient;
        try {
            sendTargetsToElastic();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Determines the type of targetFinder based on the object's sensor type
     * @param s3Key is the file for the S3Object
     * @param s3Bucket is the bucket for the S3Object
     * @param dataType is the type of sensor the S3Object comes from
     * @return the TargetFinder object
     */
    public TargetFinder determineTargetFinder(String s3Key, String s3Bucket, DataType dataType) {
        switch(dataType) {
            case DRONE:
                return new DroneTargetFinder(s3Key, s3Bucket, (DroneMetadata) metadata);
            case HANDHELD:
                return new HandheldCameraTargetFinder(s3Key, s3Bucket, (HandheldCameraMetadata) metadata);
            default:
                return null;
        }
    }

    /**
     * Sends target information to elasticsearch
     * @return a list of responses for the target information uploads
     * @throws IOException
     */
    public List<IndexResponse> sendTargetsToElastic() throws IOException {
        List<IndexResponse> responses = new ArrayList<>();
        targetFinder.updateProvenance(", Sent to Elasticsearch: ");
        for (BoundingBox box: targetFinder.getBoxes()) {
            IndexResponse response = elasticClient.postData("targets", targetFinder.getTarget(box));
            responses.add(response);
        }
        return responses;
    }

    public Metadata getMetadata() {
        return metadata;
    }
}
