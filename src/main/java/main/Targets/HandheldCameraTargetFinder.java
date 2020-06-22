package main.Targets;

import com.amazonaws.services.rekognition.model.BoundingBox;
import main.Metadata.HandheldCameraMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Target finder for handheld camera targets
 */
public class HandheldCameraTargetFinder extends TargetFinder {

    HandheldCameraMetadata metadata;

    /**
     * Creates and instance of HandheldCameraTargetFinder
     * @param s3Key is the file name of the S3Object
     * @param s3Bucket is the bucket of the S3Object
     * @param metadata is the metadata of the S3Object
     */
    public HandheldCameraTargetFinder(String s3Key, String s3Bucket, HandheldCameraMetadata metadata) {
        super(s3Key, s3Bucket, metadata);
        this.metadata = metadata;
    }

    /**
     * Calculates the target Direction relative to East (0 to Pi (1,2))
     * (0 to -Pi (3, 4)). Takes account of camera direction.
     * Will be used for the direction of the target cone.
     * @param box is the BoundingBox around the target
     * @return the direction of the target
     */
    public double calculateTargetDirection(BoundingBox box) {
        // center of bounding box
        double center = box.getLeft() + box.getWidth() / 2.0;
        // translate center to origin
        center -= 0.5;

        // target direction relative to y axis
        double directionRelativeY = metadata.getFov() * center;
        // camera direction 360 cw relative east
        double cameraDirectionEast360 = convert180to360CWRelativeEast(metadata.getYaw());
        // target direction relative camera direction
        double direction = cameraDirectionEast360 + directionRelativeY;
        direction = roundDirectionTo360(direction);

        return convert360CWto180(direction);
    }

    /**
     * Gets the width of the box. Will be used for the width of the cone
     * (and will help determine radius)
     * @param box is the BoundingBox around the target
     * @return The relative size of the target
     */
    public double calculateConeAngle(BoundingBox box)
    {
        return box.getWidth() * metadata.getFov();
    }

    /**
     * Ensures that angle is between 0 and 2Pi
     * @param direction is the angle
     * @return the corrected direction
     */
    public double roundDirectionTo360(double direction) {
        if (direction < 0) {
            direction = 2 * Math.PI + direction;
        }
        else if (direction >= 2 * Math.PI) {
            direction %= (2 * Math.PI);
        }
        return direction;
    }

    /**
     * Converts 360 CW E back to 180 E schema
     * @param angle is the original angle
     * @return the converted angle
     */
    public double convert360CWto180(double angle) {
        if (angle >= Math.PI) {
            angle = 2 * Math.PI - angle;
        }
        else {
            angle *= -1;
        }
        return angle;
    }

    /**
     * Gets json formatted data for a target
     * @param box is the BoundingBox around a target
     * @return Map<String, String> containing the data
     */
    @Override
    public Map<String, String> getTarget(BoundingBox box) {
        Map<String, String> jsonMap = new HashMap<>();
        jsonMap.put("sensor_id", metadata.getSensorID());
        jsonMap.put("sensor_type", metadata.getSensorType());
        jsonMap.put("timestamp", metadata.getTimestamp());
        jsonMap.put("location", metadata.getLatitude() + "," + metadata.getLongitude());
        jsonMap.put("cone_angle", Double.toString(calculateConeAngle(box)));
        jsonMap.put("cone_radius", "100");
        jsonMap.put("cone_direction", Double.toString(calculateTargetDirection(box)));
        updateProvenance(", Sent to Elasticsearch: ");
        jsonMap.put("provenance", metadata.getProvenance().toString());
        return jsonMap;
    }
}
