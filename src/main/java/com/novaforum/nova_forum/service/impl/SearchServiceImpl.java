package com.novaforum.nova_forum.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Highlight;
import co.elastic.clients.elasticsearch.core.search.HighlightField;
import co.elastic.clients.elasticsearch.indices.*;
import com.novaforum.nova_forum.dto.SearchRequest;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.entity.PostDocument;
import com.novaforum.nova_forum.service.SearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 搜索服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "posts";

    @Override
    public SearchResponse searchPosts(SearchRequest request) {
        try {
            long searchStartTime = System.currentTimeMillis();

            // 构建基本的搜索请求
            co.elastic.clients.elasticsearch.core.SearchRequest.Builder esRequestBuilder = new co.elastic.clients.elasticsearch.core.SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .from((request.getPage() - 1) * request.getSize())
                    .size(request.getSize());

            // 简单的关键词搜索
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                esRequestBuilder.query(q -> q
                        .multiMatch(m -> m
                                .fields("title^3", "content^1")
                                .query(request.getKeyword())));
            } else {
                // 如果没有关键词，查询所有
                esRequestBuilder.query(q -> q.matchAll(m -> m));
            }

            // 执行搜索
            co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> response = elasticsearchClient
                    .search(esRequestBuilder.build(), PostDocument.class);

            // 处理结果
            List<PostDocument> records = response.hits().hits().stream()
                    .map(hit -> hit.source())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            long total = response.hits().total() != null ? response.hits().total().value() : 0L;
            int pages = (int) Math.ceil((double) total / request.getSize());

            SearchResponse searchResponse = new SearchResponse();
            searchResponse.setRecords(records);
            searchResponse.setTotal(total);
            searchResponse.setPages((long) pages);
            searchResponse.setCurrent(request.getPage());
            searchResponse.setSize(request.getSize());
            searchResponse.setHasNext(request.getPage() < pages);
            searchResponse.setHasPrevious(request.getPage() > 1);
            searchResponse.setKeyword(request.getKeyword());
            searchResponse.setTook(System.currentTimeMillis() - searchStartTime);

            // 获取搜索建议
            if (request.getKeyword() != null && !request.getKeyword().trim().isEmpty()) {
                searchResponse.setSuggestions(getSearchSuggestions(request.getKeyword(), 5));
            }

            return searchResponse;

        } catch (IOException e) {
            log.error("搜索帖子失败", e);
            throw new RuntimeException("搜索服务暂时不可用，请稍后重试");
        }
    }

    @Override
    public void indexPost(PostDocument postDocument) {
        try {
            IndexRequest<PostDocument> request = IndexRequest.of(i -> i
                    .index(INDEX_NAME)
                    .id(postDocument.getId().toString())
                    .document(postDocument));

            elasticsearchClient.index(request);
            log.info("成功索引帖子: {}", postDocument.getId());
        } catch (IOException e) {
            log.error("索引帖子失败: {}", postDocument.getId(), e);
            throw new RuntimeException("索引帖子失败");
        }
    }

    @Override
    public void updatePostIndex(PostDocument postDocument) {
        try {
            UpdateRequest<PostDocument, PostDocument> request = UpdateRequest.of(u -> u
                    .index(INDEX_NAME)
                    .id(postDocument.getId().toString())
                    .doc(postDocument));

            elasticsearchClient.update(request, PostDocument.class);
            log.info("成功更新帖子索引: {}", postDocument.getId());
        } catch (IOException e) {
            log.error("更新帖子索引失败: {}", postDocument.getId(), e);
            throw new RuntimeException("更新帖子索引失败");
        }
    }

    @Override
    public void deletePostIndex(Long postId) {
        try {
            DeleteRequest request = DeleteRequest.of(d -> d
                    .index(INDEX_NAME)
                    .id(postId.toString()));

            elasticsearchClient.delete(request);
            log.info("成功删除帖子索引: {}", postId);
        } catch (IOException e) {
            log.error("删除帖子索引失败: {}", postId, e);
            throw new RuntimeException("删除帖子索引失败");
        }
    }

    @Override
    public void rebuildAllIndexes() {
        try {
            // 删除现有索引
            if (indexExists()) {
                deleteIndex();
            }

            // 创建新索引
            createIndex();

            // TODO: 从数据库重新导入所有数据
            // 这里应该调用数据同步服务，但为了避免循环依赖，暂时留空
            // 可以通过管理界面或定时任务来触发完整的数据同步

            log.info("索引重建完成");
        } catch (Exception e) {
            log.error("重建索引失败", e);
            throw new RuntimeException("重建索引失败");
        }
    }

    @Override
    public List<String> getSearchSuggestions(String keyword, int size) {
        // 简化的搜索建议实现
        // 实际项目中可以使用completion suggester
        return Arrays.asList(
                keyword + "教程",
                keyword + "经验分享",
                keyword + "问题解决",
                keyword + "最佳实践").subList(0, Math.min(size, 4));
    }

    @Override
    public boolean indexExists() {
        try {
            co.elastic.clients.elasticsearch.indices.ExistsRequest request = co.elastic.clients.elasticsearch.indices.ExistsRequest
                    .of(e -> e.index(INDEX_NAME));
            return elasticsearchClient.indices().exists(request).value();
        } catch (IOException e) {
            log.error("检查索引是否存在失败", e);
            return false;
        }
    }

    @Override
    public void createIndex() {
        try {
            CreateIndexRequest request = CreateIndexRequest.of(c -> c
                    .index(INDEX_NAME)
                    .mappings(m -> m
                            .properties("id", p -> p.long_(l -> l))
                            .properties("title", p -> p.text(t -> t
                                    .analyzer("standard")
                                    .searchAnalyzer("standard")))
                            .properties("content", p -> p.text(t -> t
                                    .analyzer("standard")
                                    .searchAnalyzer("standard")))
                            .properties("userId", p -> p.long_(l -> l))
                            .properties("username", p -> p.keyword(k -> k))
                            .properties("viewCount", p -> p.integer(i -> i))
                            .properties("likeCount", p -> p.integer(i -> i))
                            .properties("createTime", p -> p.date(d -> d))
                            .properties("updateTime", p -> p.date(d -> d))));

            elasticsearchClient.indices().create(request);
            log.info("成功创建索引: {}", INDEX_NAME);
        } catch (IOException e) {
            log.error("创建索引失败: {}", INDEX_NAME, e);
            throw new RuntimeException("创建索引失败");
        }
    }

    @Override
    public void deleteIndex() {
        try {
            DeleteIndexRequest request = DeleteIndexRequest.of(d -> d.index(INDEX_NAME));
            elasticsearchClient.indices().delete(request);
            log.info("成功删除索引: {}", INDEX_NAME);
        } catch (IOException e) {
            log.error("删除索引失败: {}", INDEX_NAME, e);
            throw new RuntimeException("删除索引失败");
        }
    }
}
