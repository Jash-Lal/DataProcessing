package main.Metadata;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.iterable.S3Objects;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import main.Exceptions.InvalidMetadataException;

import java.util.Map;

/**
 * Metadata that is common to all sensors
 */
abstract public class Metadata {

    private double fov;
    private double latitude;
    private double longitude;
    private String sensorID;
    private String timestamp;
    private double yaw;
    private String sensorType;
    protected StringBuilder provenance;
    protected Map<String, String> metadata;
    private S3Object s3Object;

    /**
     * Creates an instance of Metadata
     * @param s3Object is the object that triggered the lambda function
     */
    public Metadata(S3Object s3Object) {
        this.s3Object = s3Object;
        metadata = s3Object.getObjectMetadata().getUserMetadata();
        provenance = new StringBuilder();
        try {
            setMetadata();
        }
        catch (InvalidMetadataException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets metadata from the object and sets it to the fields
     * @throws InvalidMetadataException
     */
    private void setMetadata() throws InvalidMetadataException {
        if (confirmFields()) {
            fov = Double.parseDouble(metadata.get("fov"));
            latitude = Double.parseDouble(metadata.get("latitude"));
            longitude = Double.parseDouble(metadata.get("longitude"));
            sensorID = metadata.get("sensor_id");
            timestamp = metadata.get("timestamp");
            yaw = Double.parseDouble(metadata.get("yaw"));
            sensorType = metadata.get("sensor_type");
        }
        else {
            throw new InvalidMetadataException("Some necessary fields weren't included.");
        }
    }

    /**
     * Sets up the provenance field to keep track of data flow
     * @param s3 is an Amazon S3 Client
     */
    public void setProvenance(AmazonS3 s3) {
        // create iterable of s3 object summaries based on object details
        // SHOULD BE ONLY ONE MATCH
        for ( S3ObjectSummary summary : S3Objects.withPrefix(s3, s3Object.getBucketName(), s3Object.getKey()) ) {
            provenance.append("Captured: ");
            provenance.append(timestamp);
            provenance.append(", Received at S3: ");
            provenance.append(summary.getLastModified().getTime());
        }
    }

    /**
     * Confirms that necessary fields are present
     * @return boolean
     */
    private boolean confirmFields() {
        String[] fields = {"fov", "latitude", "longitude", "sensor_id", "timestamp", "yaw", "sensor_type"};
        for (String field : fields) {
            if (!metadata.containsKey(field)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter
     * @return the horizontal fov of the camera in radians
     */
    public double getFov() {
        return fov;
    }

    /**
     * Getter
     * @return the latitude in degrees
     */
    public double getLatitude() {
        return latitude;
    }

    /**
     * Getter
     * @return the longitude in degrees
     */
    public double getLongitude() {
        return longitude;
    }

    /**
     * Getter
     * @return the id corresponding to the specific sensor
     */
    public String getSensorID() {
        return sensorID;
    }

    /**
     * Getter
     * @return timestamp in Unix time
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * Getter
     * @return the direction the camera is pointing (Radians + for quadrants 1,2 - for quadrants 3,4)
     */
    public double getYaw() {
        return yaw;
    }

    /**
     * Getter
     * @return String indicating the type of sensor (e.g., drone or thermal)
     */
    public String getSensorType() {
        return sensorType;
    }

    public StringBuilder getProvenance() { return provenance; }

    /**
     * Formats metadata for upload to elasticsearch
     * @return Map<String, String> containing the data
     */
    abstract public Map<String, String> formatMetadata();
}
