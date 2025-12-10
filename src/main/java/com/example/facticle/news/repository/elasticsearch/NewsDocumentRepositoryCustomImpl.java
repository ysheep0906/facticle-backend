package com.example.facticle.news.repository.elasticsearch;

import com.example.facticle.news.entity.NewsDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchPhraseQuery;

@Slf4j
@Repository
@RequiredArgsConstructor
public class NewsDocumentRepositoryCustomImpl
        implements NewsDocumentRepositoryCustom {

    private final ElasticsearchTemplate elasticsearchTemplate;

    @Override
    public List<Long> searchByTitle(List<String> keywords) {

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery().must(
                        keywords.stream()
                                .map(k -> matchPhraseQuery("title", k))
                                .toList()
                ))
                .build();

        SearchHits<NewsDocument> hits =
                elasticsearchTemplate.search(query, NewsDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> convertToLong(hit.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> searchByContent(List<String> keywords) {

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery().must(
                        keywords.stream()
                                .map(k -> matchPhraseQuery("content", k))
                                .toList()
                ))
                .build();

        SearchHits<NewsDocument> hits =
                elasticsearchTemplate.search(query, NewsDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> convertToLong(hit.getId()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> searchByTitleOrContent(List<String> keywords) {

        NativeSearchQuery query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery().must(
                        keywords.stream()
                                .map(k -> boolQuery()
                                        .should(matchPhraseQuery("title", k))
                                        .should(matchPhraseQuery("content", k))
                                ).toList()
                ))
                .build();

        SearchHits<NewsDocument> hits =
                elasticsearchTemplate.search(query, NewsDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> convertToLong(hit.getId()))
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
