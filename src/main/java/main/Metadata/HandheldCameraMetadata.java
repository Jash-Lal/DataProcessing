package main.Metadata;

import com.amazonaws.services.s3.model.S3Object;
import main.Metadata.Metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Metadata for the handheld sensors
 */
public class HandheldCameraMetadata extends Metadata {

    /**
     * Creates an instance of HandheldCameraMetadata
     * @param s3Object is the object that triggered the lambda function
     */
    public HandheldCameraMetadata(S3Object s3Object) {
        super(s3Object);
    }

    /**
     * Formats the metadata for upload to elasticsearch
     * @return Map<String, String> containing the data
     */
    @Override
    public Map<String, String> formatMetadata() {
        Map<String, String> jsonData = new HashMap<>();
        jsonData.put("sensor_id", getSensorID());
        jsonData.put("sensor_type", getSensorType());
        jsonData.put("timestamp", getTimestamp());
        jsonData.put("location", getLatitude() + "," + getLongitude());
        jsonData.put("yaw", String.valueOf(getYaw()));
        jsonData.put("fov", String.valueOf(getFov()));
        jsonData.put("provenance", ":)");
        return jsonData;
    }

}
