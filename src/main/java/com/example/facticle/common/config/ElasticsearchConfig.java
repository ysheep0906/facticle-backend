package com.example.facticle.common.config;

import org.jetbrains.annotations.NotNull;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.transport.aws.AwsSdk2Transport;
import org.opensearch.client.transport.aws.AwsSdk2TransportOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.regions.Region;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    // ✅ OpenSearch 엔드포인트
    @Value("${opensearch.endpoint}")
    private String esHost;

    // ✅ Spring Data Elasticsearch 설정 (Rest 기반)
    @NotNull
    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(esHost.replace("https://", ""))
                .build();
    }

    // ✅ AWS IAM 기반 OpenSearch Java Client (SigV4)
    @Bean
    public OpenSearchClient openSearchClient() {

        AwsCredentialsProvider credentialsProvider =
                DefaultCredentialsProvider.create();

        SdkHttpClient httpClient =
                ApacheHttpClient.builder().build();

        // ✅ ❗ credentials는 반드시 options 안에 들어가야 함
        AwsSdk2TransportOptions options =
                AwsSdk2TransportOptions.builder()
                        .setCredentials(credentialsProvider)
                        .build();

        AwsSdk2Transport transport = new AwsSdk2Transport(
                httpClient,
                esHost,                         // ✅ https://xxxx.aoss.amazonaws.com
                Region.AP_NORTHEAST_2,
                options                          // ✅ 여기!
        );

        return new OpenSearchClient(transport);
    }
}
