package com.thed.zephyr.capture;

import com.thed.zephyr.capture.util.ApplicationConstants;
import com.thed.zephyr.capture.util.CaptureUtil;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.xml.FilterBuilder;
import org.elasticsearch.action.admin.indices.alias.IndicesAliasesRequest;
import org.elasticsearch.action.admin.indices.close.CloseIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequestBuilder;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.IndicesAdminClient;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.cluster.metadata.AliasAction;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.xcontent.ToXContent;
import org.elasticsearch.common.xcontent.XContentBuilder;
import org.elasticsearch.common.xcontent.XContentFactory;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.AliasQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import java.io.IOException;
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

        createIndex(client, ApplicationConstants.ES_INDEX_NAME);
        //Give to ES time to create index
        Thread.sleep(500);
        client.admin().indices().close(new CloseIndexRequest(ApplicationConstants.ES_INDEX_NAME));
        client.admin().indices().prepareUpdateSettings(ApplicationConstants.ES_INDEX_NAME).setSettings(Settings.builder().loadFromSource(XContentFactory.jsonBuilder()
                .startObject().startObject("analysis").startObject("analyzer").startObject("case_insensitive_sort")
                .field("tokenizer", "keyword").field("filter", "lowercase").string()).build()).get();
        client.admin().indices().open(new OpenIndexRequest(ApplicationConstants.ES_INDEX_NAME));

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

    private void createIndex(Client client, String indexName){
        GetIndexResponse getIndexResponse = client.admin().indices().getIndex(new GetIndexRequest()).actionGet();
        if(!ArrayUtils.contains(getIndexResponse.indices(), indexName)){
            CreateIndexRequest createIndexRequest = new CreateIndexRequest(indexName);
            client.admin().indices().create(createIndexRequest).actionGet();
        }
    }
}
