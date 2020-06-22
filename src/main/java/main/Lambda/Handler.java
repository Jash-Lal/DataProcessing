package main.Lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import main.DataType.DataType;
import main.Targets.Detector;
import main.Elastic.ElasticClient;
import main.Exceptions.InvalidIndexException;
import main.Metadata.MetadataSender;

/**
 * Lambda function class that is triggered by objects added to an S3 Bucket
 * Uploads metadata and target information of objects to elasticsearch
 */
public class Handler implements RequestHandler<S3Event, String> {

    /**
     * Gets invoked when an S3Object is added to an S3Bucket.
     * The object is accessed and its information is sent to the MetadataSender and the Detector
     * @param event is the S3Event that occurred
     * @param ctx is the information about the event
     * @return null
     */
    public String handleRequest(S3Event event, Context ctx) {
        // Sets up s3 client with default credentials and location
        AmazonS3 s3Client = AmazonS3ClientBuilder.defaultClient();
        ElasticClient elasticClient = new ElasticClient();
        // Event records stored in list
        for (S3EventNotification.S3EventNotificationRecord record : event.getRecords()) {
            // record.getEventTime()
            String s3Key = record.getS3().getObject().getKey();
            String s3Bucket = record.getS3().getBucket().getName();
            DataType dataType;
            try {
                dataType = determineDatatype(s3Key);
            }
            catch(InvalidIndexException e) {
                e.printStackTrace();
                continue;
            }
            MetadataSender metadataSender = new MetadataSender(s3Key, s3Bucket, dataType, elasticClient);
            Detector detector = new Detector(s3Key, s3Bucket, dataType, metadataSender.getMetadata(), elasticClient);
        }
        return null;
    }

    /**
     * Used to determine what type of sensor is provided to create appropriate datatypes
     * @param s3Key is the file name of the object added to the bucket
     * @return a DataType object
     * @throws InvalidIndexException
     */
    public DataType determineDatatype(String s3Key) throws InvalidIndexException {
        int endIndex = s3Key.indexOf('/');
        if (endIndex == -1) {
            return DataType.OTHER;
        }
        String index = s3Key.substring(0, endIndex);
        if (index.equals("droneIngest")) {
            return DataType.DRONE;
        }
        else if (index.equals("groundIngest")) {
            return DataType.HANDHELD;
        }
        else {
            throw new InvalidIndexException("The S3Object is from an invalid index.");
        }
    }
}
