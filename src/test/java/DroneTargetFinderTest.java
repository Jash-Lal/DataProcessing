import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import main.Metadata.DroneMetadata;
import main.Targets.DroneTargetFinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class DroneTargetFinderTest {

    private static DroneMetadata metadata;
    private static DroneTargetFinder targetFinder;

    @BeforeAll
    static void setup() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        S3Object object = client.getObject("s3triggertest", "droneIngest/person_center.jpg");
        metadata = new DroneMetadata(object);
        targetFinder = new DroneTargetFinder("droneIngest/person_center.jpg", "s3triggertest", metadata);
    }

    @Test
    void droneMetadataTest() {
        assertEquals(75.5, metadata.getFov(), 0.01);
        assertEquals(100, metadata.getAltitude(), 0.01);
    }

    @Test
    void convert180to360CWRelativeEastTest() {
        assertEquals(0, targetFinder.convert180to360CWRelativeEast(0), 0.01);
        assertEquals(Math.PI / 2.0, targetFinder.convert180to360CWRelativeEast(-Math.PI / 2.0), 0.01);
        assertEquals(Math.PI, targetFinder.convert180to360CWRelativeEast(-Math.PI), 0.01);
        assertEquals(Math.PI, targetFinder.convert180to360CWRelativeEast(Math.PI), 0.01);
        assertEquals(1.5 * Math.PI, targetFinder.convert180to360CWRelativeEast(0.5 * Math.PI), 0.01);
    }

    @Test
    void convert360CWEastToNorthTest() {
        assertEquals(0.5 * Math.PI, targetFinder.convert360CWEastToNorth(0), 0.01);
        assertEquals(1.5  * Math.PI, targetFinder.convert360CWEastToNorth(Math.PI), 0.01);
        assertEquals(0, targetFinder.convert360CWEastToNorth(1.5 * Math.PI), 0.01);
        assertEquals(0.25 * Math.PI, targetFinder.convert360CWEastToNorth(1.75 * Math.PI));
    }

    @Test
    void calculateDroneTargetDirectionTest() {
        assertEquals(0.75 * Math.PI, targetFinder.calculateDroneTargetDirection(0.5, 0.5), 0.01);
        assertEquals(1.25 * Math.PI, targetFinder.calculateDroneTargetDirection(0.5, -0.5), 0.01);
        assertEquals(1.75 * Math.PI, targetFinder.calculateDroneTargetDirection(-0.5, -0.5), 0.01);
        assertEquals(0.25 * Math.PI, targetFinder.calculateDroneTargetDirection(-0.5, 0.5), 0.01);
    }

    @Test
    void calculateCoordinateTest() {
        double[] coor1 = targetFinder.calculateCoordinate(100, 0);
        assertTrue(coor1[0] > 0);
        assertEquals(coor1[1], 0, 0.01);
        double[] coor2 = targetFinder.calculateCoordinate(100, 0.5 * Math.PI);
        assertEquals(0, coor2[0], 0.01);
        assertTrue(coor2[1] > 0);
        double[] coor3 = targetFinder.calculateCoordinate(10000, Math.PI);
        assertEquals(0, coor3[1], 0.01);
        assertTrue(coor3[0] < 0);
        double[] coor4 = targetFinder.calculateCoordinate(100, 1.5 * Math.PI);
        assertEquals(0, coor4[0], 0.01);
        assertTrue(coor4[1] < 0);
    }

    @Test
    void calculateDroneTargetCoordinateTest() {
        List<BoundingBox> boxes = targetFinder.getBoxes();
        BoundingBox box1 = boxes.get(0);
        double[] coor1 = targetFinder.calculateDroneTargetCoordinate(box1);
        assertTrue(coor1[0] > 0);
        assertTrue(coor1[1] < 0);
    }

    @Test
    void getTargetsTest() {
        List<BoundingBox> boxes = targetFinder.getBoxes();
        System.out.println(targetFinder.getTarget(boxes.get(0)).toString());
    }
}