package main.Targets;

import com.amazonaws.services.rekognition.model.BoundingBox;
import main.Metadata.DroneMetadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Target finder for the drone type
 */
public class DroneTargetFinder extends TargetFinder {

    DroneMetadata metadata;
    private static final double earthRadius = 6378.137;
    private double droneDirectionRelativeNorth;

    /**
     * Creates an instance of DroneTargetFinder
     * @param s3Key is the file name of the S3Object
     * @param s3Bucket is the bucket of the S3Object
     * @param metadata is the metadata for the S3Object
     */
    public DroneTargetFinder(String s3Key, String s3Bucket, DroneMetadata metadata) {
        super(s3Key, s3Bucket, metadata);
        this.metadata = metadata;
        double convertedDroneDirection = convert180to360CWRelativeEast(metadata.getYaw());
        this.droneDirectionRelativeNorth = convert360CWEastToNorth(convertedDroneDirection);
    }

    /**
     * Given a bounding box for a target, this method uses sensor metadata to get GPS coordinates of the target
     * @param box is a box that bounds the target in the image
     * @return The estimated GPS coordinates of the target
     */
    public double[] calculateDroneTargetCoordinate(BoundingBox box) {
        // coordinates of center of bounding box
        double imageX = box.getLeft() + box.getWidth() / 2.0;
        double imageY = box.getTop() + box.getHeight() / 2.0;

        // translate center of bounding box to the origin
        imageX -= 0.5;
        imageY = 0.5 - imageY;

        // distance to target (meters)
        double targetDistance = calculateDroneTargetDistance(imageX, imageY);
        double targetDirection = calculateDroneTargetDirection(imageX, imageY);
        return calculateCoordinate(targetDistance, targetDirection);
    }

    /**
     * Get actual distance to target from image x and y (at Origin)
     * @param x is the x coordinate
     * @param y is the y coordinate
     * @return The distance in meters
     */
    public double calculateDroneTargetDistance(double x, double y) {
        // Temporary hardcoded fov
        // FOV in degrees. 4:3 aspect ratio for jpeg rect photos
        // Info found at: https://www.parrot.com/files/s3fs-public/firmware/anafi_user_guide_v2.6.2.pdf
        double horizontalFov = 75.5;
        double verticalFov = 56.625;

        // Arc length calculation using altitude and fov
        double horizontalImageLength = 2 * Math.PI * metadata.getAltitude() * (horizontalFov / 360);
        double verticalImageLength = 2 * Math.PI * metadata.getAltitude() * (verticalFov / 360);

        // x and y displacements
        double disX = x * horizontalImageLength;
        double disY = y * verticalImageLength;

        // pythagorean distance
        return Math.sqrt(Math.pow(disX, 2) + Math.pow(disY, 2));
    }

    /**
     * Find the direction of the target (relative to the direction of the drone in radians (0- 2 Pi))
     * @param x is the x-coordinate of the target
     * @param y is the y-coordinate of the target
     * @return
     */
    public double calculateDroneTargetDirection(double x, double y) {
        // angle of point relative to positive x
        // Q1 and Q2: 0 to Pi     Q3 and Q4: 0 to -PI
        double angle = Math.atan2(y, x);
        double convertedTargetAngle = convert180to360CWRelativeEast(angle);
        double convertedDroneDirection = convert180to360CWRelativeEast(metadata.getYaw());
        double targetAngleNorth = convert360CWEastToNorth(convertedTargetAngle);
        double droneDirectionNorth = convert360CWEastToNorth(convertedDroneDirection);
        return (targetAngleNorth + droneDirectionNorth) % (2 * Math.PI);
    }

    /**
     * Convert 360 degree relative east angles to relative north angles
     * @param angle
     * @return is the converted angle
     */
    public double convert360CWEastToNorth(double angle) {
        return (angle >= 0 && angle < 1.5 * Math.PI) ? (angle + 0.5 * Math.PI) : (angle - 1.5 * Math.PI);
    }

    /**
     * Calculates the longitude and latitude (in radians) based on an x and y coordinate relative to the position
     * of the drone
     * @param distance is the distance from the drone
     * @param direction is the direction relative north (0 - 2Pi)
     * @return the gps coordinate (in radians) of a target
     */
    public double[] calculateCoordinate(double distance, double direction) {
        // get latitude and longitude of target
        // 	φ2 = asin( sin φ1 ⋅ cos δ + cos φ1 ⋅ sin δ ⋅ cos θ )
        // 	λ2 = λ1 + atan2( sin θ ⋅ sin δ ⋅ cos φ1, cos δ − sin φ1 ⋅ sin φ2 )
        // where	φ is latitude, λ is longitude, θ is the bearing (clockwise from north),
        // δ is the angular distance d/R; d being the distance travelled, R the earth’s radius
        double angularDistance = (distance * 0.001) / this.earthRadius;
        double lat1 = convertDegreesToRadians(metadata.getLatitude());
        double long1 = convertDegreesToRadians(metadata.getLongitude());
        double lat2 = Math.asin(Math.sin(lat1) * Math.cos(angularDistance) +
                Math.cos(lat1) * Math.sin(angularDistance) * Math.cos(direction));
        double long2 = long1 + Math.atan2(Math.sin(direction) * Math.sin(angularDistance) *
                Math.cos(lat1), Math.cos(angularDistance) - Math.sin(lat1) * Math.sin(lat2));
        double[] coordinates = new double[] {lat2, long2};
        return coordinates;
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
        double[] target = calculateDroneTargetCoordinate(box);
        jsonMap.put("location", convertRadiansToDegrees(target[0]) + "," + convertRadiansToDegrees(target[1]));
        jsonMap.put("provenance", metadata.getProvenance().toString());
        return jsonMap;
    }
}
