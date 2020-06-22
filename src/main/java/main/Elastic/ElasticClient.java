package main.Elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;

import java.io.IOException;
import java.util.Map;

/**
 * Creates a HighLevel Rest client for working with an elasticsearch cluster on an AWS EC2
 */
public class ElasticClient {

    private static final String EC2_PUBLIC_IP = "18.223.97.147";
    private RestHighLevelClient client;

    /**
     * Creates an instance of ElasticClient
     */
    public ElasticClient() {
        client = new RestHighLevelClient(RestClient.builder(
                new HttpHost(EC2_PUBLIC_IP, 9200, "http")
        ));
    }

    /**
     * Returns the client
     * @return RestHighLevelClient
     */
    public RestHighLevelClient getClient() {
        return client;
    }

    /**
     * Posts data to the client
     * @param index is the elasticsearch index to post to
     * @param data is a HashMap containing data to post
     * @return IndexResponse for the upload
     * @throws IOException
     */
    public IndexResponse postData(String index, Map<String, String> data) throws IOException {
        IndexRequest indexRequest = new IndexRequest(index).source(data);
        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        return indexResponse;
    }
}