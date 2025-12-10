package com.example.facticle.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${opensearch.endpoint}")
    private String esHost;

    @Override
    public ClientConfiguration clientConfiguration() {

        String hostWithoutProtocol = esHost.replace("https://", "");

        return ClientConfiguration.builder()
                .connectedTo(hostWithoutProtocol + ":443")   // ⭐ 포트 명시 필수
                .usingSsl()                                  // ⭐ AWS OpenSearch는 무조건 SSL
                .build();
    }
}
