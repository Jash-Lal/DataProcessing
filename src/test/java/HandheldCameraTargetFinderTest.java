import com.amazonaws.services.rekognition.model.BoundingBox;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import main.Metadata.HandheldCameraMetadata;
import main.Targets.Detector;
import main.Targets.HandheldCameraTargetFinder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

public class HandheldCameraTargetFinderTest {

    private static HandheldCameraMetadata metadata;
    private static HandheldCameraTargetFinder targetFinder;
    private static Detector detector;

    @BeforeAll
    static void setup() {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        S3Object object = client.getObject("s3triggertest", "groundIngest/thermal-1592399997905.png");
        metadata = new HandheldCameraMetadata(object);
        targetFinder = new HandheldCameraTargetFinder("groundIngest/thermal-1592399997905.png", "s3triggertest", metadata);
    }

    @Test
    void roundDirectionTo360Test() {
        assertEquals(1.75 * Math.PI, targetFinder.roundDirectionTo360(-0.25 * Math.PI), 0.01);
        assertEquals(Math.PI, targetFinder.roundDirectionTo360(Math.PI), 0.01);
        assertEquals(0.25 * Math.PI, targetFinder.roundDirectionTo360(2.25 * Math.PI), 0.01);
    }

    @Test
    void convert360CWto180Test() {
        assertEquals(Math.PI, targetFinder.convert360CWto180(Math.PI), 0.01);
        assertEquals(0.5 * Math.PI, targetFinder.convert360CWto180(1.5 * Math.PI), 0.01);
        assertEquals(0, targetFinder.convert360CWto180(0), 0.01);
        assertEquals(-0.5 * Math.PI, targetFinder.convert360CWto180(0.5 * Math.PI), 0.01);
        assertEquals(-0.25 * Math.PI, targetFinder.convert360CWto180(0.25 * Math.PI), 0.01);
    }

    @Test
    void calculateTargetDirectionTest() {
        List<BoundingBox> boxes = targetFinder.getBoxes();
        BoundingBox box = boxes.get(0);
        System.out.println(box.getLeft());
        System.out.println(box.getTop());
        System.out.println();
        assertTrue(targetFinder.calculateTargetDirection(box) > metadata.getYaw());
    }

    @Test
    void testGetTargets() {
        List<BoundingBox> boxes = targetFinder.getBoxes();
        System.out.println(targetFinder.getTarget(boxes.get(0)));
    }
}

