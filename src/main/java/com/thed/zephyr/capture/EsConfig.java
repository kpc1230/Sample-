package com.thed.zephyr.capture;

import com.thed.zephyr.capture.util.ApplicationConstants;
import org.apache.commons.lang3.ArrayUtils;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.net.InetAddress;

/**
 * Created by aliakseimatsarski on 8/29/17.
 */
@Configuration
@EnableElasticsearchRepositories(basePackages = "com.thed.zephyr.capture.repositories.elasticsearch")
public class EsConfig {

    @Autowired
    private Logger log;

    private static Integer index = 0;

    @Value("${elasticsearch.host}")
    private String EsHost;

    @Value("${elasticsearch.port}")
    private int EsPort;

    @Value("${elasticsearch.clustername}")
    private String EsClusterName;

    @Bean
    public Client client() throws Exception {

        Settings esSettings = Settings.settingsBuilder()
                .put("cluster.name", EsClusterName)
                .build();

        //https://www.elastic.co/guide/en/elasticsearch/guide/current/_transport_client_versus_node_client.html
        Client client = TransportClient.builder()
                .settings(esSettings)
                .build()
                .addTransportAddress(
                        new InetSocketTransportAddress(InetAddress.getByName(EsHost), EsPort));

        GetIndexResponse getIndexResponse = client.admin().indices().getIndex(new GetIndexRequest()).actionGet();
        if(!ArrayUtils.contains(getIndexResponse.indices(), ApplicationConstants.ES_INDEX_NAME)){
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(ApplicationConstants.ES_INDEX_NAME);
            client.admin().indices().create(createIndexRequest).actionGet();
            //Give to ES time to create index
            Thread.sleep(500);
        }
        client.admin().indices().close(new CloseIndexRequest(EsClusterName));
        client.admin().indices().prepareUpdateSettings(EsClusterName).setSettings(Settings.builder().loadFromSource(XContentFactory.jsonBuilder()
        		.startObject().startObject("analysis").startObject("analyzer").startObject("case_insensitive_sort")
        		.field("tokenizer", "keyword").field("filter", "lowercase").string()).build()).get();
        client.admin().indices().open(new OpenIndexRequest(EsClusterName));

        return client; 
    }

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate() throws Exception {
        return new ElasticsearchTemplate(client());
    }


    //Embedded Elasticsearch Server
    /*@Bean
    public ElasticsearchOperations elasticsearchTemplate() {
        return new ElasticsearchTemplate(nodeBuilder().local(true).node().client());
    }*/
}
