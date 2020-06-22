import main.DataType.DataType;
import main.Elastic.ElasticClient;
import main.Metadata.MetadataSender;
import main.Targets.Detector;
import org.elasticsearch.action.index.IndexResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ElasticClientTest {

    private static ElasticClient elasticClient;
    private static MetadataSender droneMetadataSender;
    private static Detector droneDetector;
    private static MetadataSender handheldMetadataSender;
    private static Detector handheldDetector;

    @BeforeAll
    static void setUp() {
        elasticClient = new ElasticClient();
        droneMetadataSender = new MetadataSender("droneIngest/person_center.jpg", "s3triggertest",
                DataType.DRONE, elasticClient);
        droneDetector = new Detector("droneIngest/person_center.jpg", "s3triggertest",
                DataType.DRONE, droneMetadataSender.getMetadata(), elasticClient);
        handheldMetadataSender = new MetadataSender("groundIngest/thermal-1592399997905.png", "s3triggertest",
                DataType.HANDHELD, elasticClient);
        handheldDetector = new Detector("groundIngest/thermal-1592399997905.png", "s3triggertest",
                DataType.HANDHELD, handheldMetadataSender.getMetadata(), elasticClient);
    }

    @Test
    void sendMetadataToElasticTest() {
        try {
            IndexResponse indexResponse = droneMetadataSender.sendMetadataToElastic();
            System.out.println(indexResponse.status());
            System.out.println(indexResponse.getIndex());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void sendTargetsToElasticTest() {
        try {
            droneDetector.sendTargetsToElastic();
            System.out.println(droneDetector.getMetadata().getProvenance().toString());
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void sendTargetsToElasticHandheldTest() {
        try {
            handheldDetector.sendTargetsToElastic();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    void droneProvenanceTest() {
        System.out.println(droneDetector.getMetadata().getProvenance().toString());
    }
}
