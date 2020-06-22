package main.Targets;

import com.amazonaws.services.rekognition.AmazonRekognition;
import com.amazonaws.services.rekognition.AmazonRekognitionClientBuilder;
import com.amazonaws.services.rekognition.model.*;
import main.Metadata.Metadata;

import java.lang.Math.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Abstract class for TargetFinder subclasses
 */
abstract public class TargetFinder {

    protected List<Label> labels;
    private String s3Key;
    private String s3Bucket;
    private Metadata metadata;

    /**
     * Superclass constructor
     * @param s3Key is the file name of the S3Object
     * @param s3Bucket is the bucket of the S3Object
     */
    public TargetFinder(String s3Key, String s3Bucket, Metadata metadata) {
        this.s3Key = s3Key;
        this.s3Bucket = s3Bucket;
        this.metadata = metadata;
        this.labels = detect();
    }

    /**
     * Creates a Rekognition client and calls teh detectLabels() function.
     * @return List<Label> this list of labels
     */
    public List<Label> detect() {
        AmazonRekognition rekognitionClient = AmazonRekognitionClientBuilder.defaultClient();
        updateProvenance(", Sent to Rekognition: ");
        DetectLabelsRequest request = new DetectLabelsRequest().withImage(new Image()
                .withS3Object(new S3Object().withName(s3Key).withBucket(s3Bucket))).withMinConfidence(50F);
        updateProvenance(", Received from Rekognition: ");
        DetectLabelsResult result = rekognitionClient.detectLabels(request);
        List<Label> labels = result.getLabels();
        return labels;
    }

    /**
     * Update the provenance field with the provided body and current time
     * @param body
     */
    public void updateProvenance(String body) {
        StringBuilder provenance = metadata.getProvenance();
        provenance.append(body);
        long unixTime = Instant.now().toEpochMilli();
        provenance.append(unixTime);
    }

    /**
     * Scans through Rekognition labels for Human and Person labels.
     *
     * @return Bounding boxes for the targets identified
     */
    public List<BoundingBox> getBoxes() {
        List<BoundingBox> boxes = new ArrayList<BoundingBox>();
        for (Label label : labels) {
            String name = label.getName();
            if (name.equals("Human") || name.equals("Person")) {
                for (Instance instance : label.getInstances()) {
                    boxes.add(instance.getBoundingBox());
                }
            }
        }
        return boxes;
    }

    /**
     * Convert degrees to radians
     * @param angle in degrees
     * @return angle in radians
     */
    public double convertDegreesToRadians(double angle) {
        return angle * (Math.PI / 180.0);
    }

    /**
     * Convert radians to degrees
     * @param angle in radians
     * @return angle in degrees
     */
    public double convertRadiansToDegrees(double angle) { return angle * (180.0 / Math.PI); }

    /**
     * Convert 180 degree angle CW relative east to 360 degree CCW relative east
     * @param angle
     * @return
     */
    public double convert180to360CWRelativeEast(double angle) {
        return (angle > 0) ? (2 * Math.PI - angle) : -angle;
    }

    /**
     * Getter
     * @return List<Label> the list of labels for detected objects
     */
    public List<Label> getLabels() {
        return labels;
    }

    /**
     * Returns the json formatted target data
     * @param box is the BoundingBox around the target
     * @return Map<String, String> containing the data
     */
    abstract public Map<String, String> getTarget(BoundingBox box);
}
