package com.example.facticle.news.repository.elasticsearch;

import com.example.facticle.news.entity.NewsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsDocumentRepositoryCustomImpl implements NewsDocumentRepositoryCustom {

    // ✅ Spring Data Elasticsearch 정석
    private final ElasticsearchOperations elasticsearchOperations;

    @Override
    public List<Long> searchByTitle(List<String> keywords) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        keywords.forEach(k ->
                boolQuery.must(QueryBuilders.matchPhraseQuery("title", k))
        );

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        return execute(query);
    }

    @Override
    public List<Long> searchByContent(List<String> keywords) {

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        keywords.forEach(k ->
                boolQuery.must(QueryBuilders.matchPhraseQuery("content", k))
        );

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .build();

        return execute(query);
    }

    @Override
    public List<Long> searchByTitleOrContent(List<String> keywords) {

        BoolQueryBuilder outerBool = QueryBuilders.boolQuery();

        keywords.forEach(k -> {
            BoolQueryBuilder innerBool = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchPhraseQuery("title", k))
                    .should(QueryBuilders.matchPhraseQuery("content", k));

            outerBool.must(innerBool);
        });

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(outerBool)
                .build();

        return execute(query);
    }

    private List<Long> execute(NativeSearchQuery query) {

        return elasticsearchOperations
                .search(query, NewsDocument.class)
                .stream()
                .map(SearchHit::getId)
                .map(this::convertToLong)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Long convertToLong(String newsId) {
        try {
            return newsId != null ? Long.parseLong(newsId) : null;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
