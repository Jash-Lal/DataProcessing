package main.Metadata;

import com.amazonaws.services.s3.model.S3Object;
import main.Exceptions.InvalidMetadataException;
import main.Metadata.Metadata;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Metadata for the drone sensor
 */
public class DroneMetadata extends Metadata {

    private double altitude;

    /**
     * Creates a new instance of DroneMetadata
     * @param s3Object is the object that triggered the lambda function
     */
    public DroneMetadata(S3Object s3Object) {
        super(s3Object);
        try {
            setMetadata();
        }
        catch (InvalidMetadataException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets additional metadata from the object and sets the fields
     * @throws InvalidMetadataException
     */
    private void setMetadata() throws InvalidMetadataException {
        if (confirmAdditionalFields(metadata)) {
            altitude = Double.parseDouble(metadata.get("altitude"));
        }
        else {
            throw new InvalidMetadataException("Some necessary fields weren't included.");
        }
    }

    /**
     * Confirms that additional fields are provided
     * @param metadata is the objects metadata
     * @return whether or not the additional fields are present
     */
    public boolean confirmAdditionalFields(Map<String,String> metadata) {
        String[] fields = {"altitude"};
        for (String field : fields) {
            if (!metadata.containsKey("altitude")) {
                return false;
            }
        }
        return true;
    }

    /**
     * Getter
     * @return altitude
     */
    public double getAltitude() {
        return altitude;
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
        jsonData.put("altitude", String.valueOf(altitude));
        jsonData.put("provenance", provenance.toString() + ", Sent to Elasticsearch: " + Instant.now().toEpochMilli());
        return jsonData;
    }
}
