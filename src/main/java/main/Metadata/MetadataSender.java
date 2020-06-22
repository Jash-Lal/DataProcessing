package main.Metadata;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import main.DataType.DataType;
import main.Elastic.ElasticClient;
import org.elasticsearch.action.index.IndexResponse;

import java.io.IOException;
import java.util.Map;

/**
 * Sends metadata to the sensors index in elasticsearch
 */
public class MetadataSender {

    private S3Object s3Object;
    private Metadata metadata;
    private ElasticClient elasticClient;

    /**
     * Creates an instance of MetadataSender
     * @param s3Key is the S3object file name
     * @param s3Bucket is the bucket of the S3Object
     * @param dataType is the type of sensor the object comes from
     * @param elasticClient is the elasticsearch client
     */
    public MetadataSender(String s3Key, String s3Bucket, DataType dataType, ElasticClient elasticClient) {
        AmazonS3 client = AmazonS3ClientBuilder.defaultClient();
        this.s3Object = client.getObject(s3Bucket, s3Key);
        this.metadata = determineMetadata(dataType);
        metadata.setProvenance(client);
        this.elasticClient = elasticClient;
        try {
            sendMetadataToElastic();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates the appropriate type of metadata for the object's sensor
     * @param dataType
     * @return the metadata object
     */
    public Metadata determineMetadata(DataType dataType) {
        switch(dataType) {
            case DRONE:
                return new DroneMetadata(s3Object);
            case HANDHELD:
                return new HandheldCameraMetadata(s3Object);
            default:
                return null;
        }
    }

    /**
     * Sends data to elasticsearch
     * @return the IndexResponse from elasticsearch
     * @throws IOException
     */
    public IndexResponse sendMetadataToElastic() throws IOException {

        Map<String, String> jsonMetadata = metadata.formatMetadata();
        IndexResponse indexResponse = elasticClient.postData("sensors", jsonMetadata);
        return indexResponse;
    }

    /**
     * Getter
     * @return the object's metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }
}
