package com.example.facticle.news.search;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.action.search.SearchRequest;
import org.opensearch.action.search.SearchResponse;
import org.opensearch.client.RequestOptions;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NewsSearchService {

    private final RestHighLevelClient openSearchClient;

    public List<Long> searchByTitle(List<String> keywords) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        keywords.forEach(k ->
                boolQuery.must(QueryBuilders.matchPhraseQuery("title", k))
        );

        return executeSearch(boolQuery);
    }

    public List<Long> searchByContent(List<String> keywords) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        keywords.forEach(k ->
                boolQuery.must(QueryBuilders.matchPhraseQuery("content", k))
        );

        return executeSearch(boolQuery);
    }

    public List<Long> searchByTitleOrContent(List<String> keywords) {
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        keywords.forEach(k ->
                boolQuery.should(QueryBuilders.matchPhraseQuery("title", k))
                        .should(QueryBuilders.matchPhraseQuery("content", k))
        );

        return executeSearch(boolQuery);
    }

    private List<Long> executeSearch(BoolQueryBuilder query) {
        try {
            SearchRequest request = new SearchRequest("news_index");
            request.source().query(query);

            SearchResponse response =
                    openSearchClient.search(request, RequestOptions.DEFAULT);

            return response.getHits().getHits().stream()
                    .map(hit -> {
                        try {
                            return Long.parseLong(hit.getId());
                        } catch (Exception e) {
                            return null;
                        }
                    })
                    .filter(id -> id != null)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("OpenSearch 검색 실패", e);
        }
    }
    
    private Long convertToLong(String newsId) {
        try {
            return newsId != null ? Long.parseLong(newsId) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
