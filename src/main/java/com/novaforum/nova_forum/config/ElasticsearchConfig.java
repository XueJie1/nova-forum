package com.novaforum.nova_forum.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch配置类
 */
@Slf4j
@Configuration
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUris;

    @Value("${spring.elasticsearch.username:}")
    private String username;

    @Value("${spring.elasticsearch.password:}")
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() {
        try {
            // 解析Elasticsearch URI
            String[] uriParts = elasticsearchUris.replace("http://", "").replace("https://", "").split(":");
            String host = uriParts[0];
            int port = uriParts.length > 1 ? Integer.parseInt(uriParts[1]) : 9200;

            // 创建RestClient
            RestClient restClient;
            if (username != null && !username.isEmpty() && password != null && !password.isEmpty()) {
                // 配置认证
                final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
                credentialsProvider.setCredentials(AuthScope.ANY,
                        new UsernamePasswordCredentials(username, password));

                restClient = RestClient.builder(new HttpHost(host, port, "http"))
                        .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder
                                .setDefaultCredentialsProvider(credentialsProvider))
                        .build();
            } else {
                restClient = RestClient.builder(new HttpHost(host, port, "http")).build();
            }

            // 创建自定义ObjectMapper
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.registerModule(new JavaTimeModule());

            // 创建传输层
            ElasticsearchTransport transport = new RestClientTransport(restClient,
                    new JacksonJsonpMapper(objectMapper));

            // 创建Elasticsearch客户端
            ElasticsearchClient client = new ElasticsearchClient(transport);

            log.info("Elasticsearch客户端初始化成功，连接到: {}", elasticsearchUris);
            return client;

        } catch (Exception e) {
            log.error("Elasticsearch客户端初始化失败", e);
            throw new RuntimeException("Elasticsearch连接失败", e);
        }
    }
}
