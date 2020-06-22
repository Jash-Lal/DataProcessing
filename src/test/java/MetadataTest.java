import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import main.Metadata.HandheldCameraMetadata;
import main.Metadata.Metadata;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class MetadataTest {

    private AmazonS3 client;
    private Metadata metadata1;

    public MetadataTest() {
        client = AmazonS3ClientBuilder.defaultClient();
        S3Object object = client.getObject("s3triggertest","groundIngest/thermal-1592399997905.png");
        metadata1 = new HandheldCameraMetadata(object);
    }

    @Test
    void getFieldsTest() {
        assertEquals(metadata1.getSensorID(), 3);
    }

    @Test
    void getProvenanceTest() {
        metadata1.setProvenance(client);
        System.out.println(metadata1.getProvenance().toString());
    }
}
