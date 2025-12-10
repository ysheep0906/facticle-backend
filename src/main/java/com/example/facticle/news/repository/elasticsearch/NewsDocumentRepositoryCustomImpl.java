package com.example.facticle.news.repository.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.MatchPhraseQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.example.facticle.news.entity.NewsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsDocumentRepositoryCustomImpl implements NewsDocumentRepositoryCustom {

    private final ElasticsearchClient elasticsearchClient;

    @Override
    public List<Long> searchByTitle(List<String> keywords) {

        List<Query> mustQueries = keywords.stream()
                .map(k -> Query.of(q -> q.matchPhrase(
                        MatchPhraseQuery.of(m -> m.field("title").query(k))
                )))
                .toList();

        return executeSearch(mustQueries);
    }

    @Override
    public List<Long> searchByContent(List<String> keywords) {

        List<Query> mustQueries = keywords.stream()
                .map(k -> Query.of(q -> q.matchPhrase(
                        MatchPhraseQuery.of(m -> m.field("content").query(k))
                )))
                .toList();

        return executeSearch(mustQueries);
    }

    @Override
    public List<Long> searchByTitleOrContent(List<String> keywords) {

        List<Query> mustQueries = keywords.stream()
                .map(k -> Query.of(q -> q.bool(
                        BoolQuery.of(b -> b
                                .should(Query.of(s -> s.matchPhrase(
                                        MatchPhraseQuery.of(m -> m.field("title").query(k))
                                )))
                                .should(Query.of(s -> s.matchPhrase(
                                        MatchPhraseQuery.of(m -> m.field("content").query(k))
                                )))
                        )
                )))
                .toList();

        return executeSearch(mustQueries);
    }

    private List<Long> executeSearch(List<Query> mustQueries) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index("news_index")
                    .query(q -> q.bool(b -> b.must(mustQueries)))
                    .build();

            SearchResponse<NewsDocument> response =
                    elasticsearchClient.search(searchRequest, NewsDocument.class);

            return response.hits().hits().stream()
                    .map(hit -> convertToLong(hit.id()))
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new RuntimeException("OpenSearch 검색 오류", e);
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
