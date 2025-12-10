package com.example.facticle.common.config;

import org.jetbrains.annotations.NotNull;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    // ✅ 기존 변수명 그대로 유지 (다른 설정들과 충돌 방지)
    @Value("${opensearch.endpoint}")
    private String esHost;

    // ✅ 함수명 그대로 유지
    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(esHost.replace("https://", ""))  // spring-data-elasticsearch 호환용
                .build();
    }

    // ✅ 실제 OpenSearch Serverless 클라이언트 (IAM 인증)
    @Bean
    public OpenSearchClient openSearchClient() {

        AwsCredentialsProvider credentialsProvider =
                DefaultCredentialsProvider.create();

        AwsSdk2Transport transport = new AwsSdk2Transport(
                ApacheHttpClient.builder().build(),
                esHost,                         // ✅ https://xxxx.aoss.amazonaws.com
                Region.AP_NORTHEAST_2,
                credentialsProvider
        );

        return new OpenSearchClient(transport);
    }
}
