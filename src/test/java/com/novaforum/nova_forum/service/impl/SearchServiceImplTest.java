package com.novaforum.nova_forum.service.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Result;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.HitsMetadata;
import co.elastic.clients.elasticsearch.core.search.TotalHits;
import co.elastic.clients.elasticsearch.core.search.TotalHitsRelation;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.DeleteIndexRequest;
import co.elastic.clients.elasticsearch.indices.DeleteIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.transport.endpoints.BooleanResponse;
import com.novaforum.nova_forum.dto.SearchRequest;
import com.novaforum.nova_forum.dto.SearchResponse;
import com.novaforum.nova_forum.entity.PostDocument;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * SearchServiceImpl 单元测试
 *
 * 测试覆盖：
 * - 搜索功能（关键词、空关键词、分页）
 * - 索引管理（创建、删除、检查存在）
 * - 文档操作（索引、更新、删除）
 * - 搜索建议
 * - 异常处理
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("搜索服务单元测试")
class SearchServiceImplTest {

    @Mock
    private ElasticsearchClient elasticsearchClient;

    @Mock
    private ElasticsearchIndicesClient indicesClient;

    @InjectMocks
    private SearchServiceImpl searchService;

    private PostDocument testPost;
    private SearchRequest searchRequest;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testPost = new PostDocument();
        testPost.setId(1L);
        testPost.setTitle("Java编程基础教程");
        testPost.setContent("这是一篇关于Java编程的基础教程，涵盖语法、面向对象等内容");
        testPost.setUserId(100L);
        testPost.setUsername("testuser");
        testPost.setViewCount(1000);
        testPost.setLikeCount(50);
        testPost.setCreateTime(LocalDateTime.now());
        testPost.setUpdateTime(LocalDateTime.now());

        searchRequest = new SearchRequest();
        searchRequest.setKeyword("Java");
        searchRequest.setPage(1);
        searchRequest.setSize(10);

        // Mock indices client
        when(elasticsearchClient.indices()).thenReturn(indicesClient);
    }

    // ==================== 搜索功能测试 ====================

    @Test
    @DisplayName("测试关键词搜索 - 成功返回结果")
    void testSearchPosts_WithKeyword_Success() throws IOException {
        // Arrange
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Arrays.asList(testPost), 1L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getTitle()).contains("Java");
        assertThat(result.getTotal()).isEqualTo(1L);
        assertThat(result.getPages()).isEqualTo(1L);
        assertThat(result.getCurrent()).isEqualTo(1);
        assertThat(result.getSize()).isEqualTo(10);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getHasPrevious()).isFalse();
        assertThat(result.getKeyword()).isEqualTo("Java");
        assertThat(result.getSuggestions()).isNotEmpty();
        assertThat(result.getTook()).isGreaterThanOrEqualTo(0L);

        // Verify
        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试空关键词搜索 - 返回所有结果")
    void testSearchPosts_WithEmptyKeyword_ReturnsAll() throws IOException {
        // Arrange
        searchRequest.setKeyword("");
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Arrays.asList(testPost), 1L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getSuggestions()).isNull(); // 空关键词不生成建议

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试 null 关键词搜索 - 返回所有结果")
    void testSearchPosts_WithNullKeyword_ReturnsAll() throws IOException {
        // Arrange
        searchRequest.setKeyword(null);
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Arrays.asList(testPost), 1L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).hasSize(1);

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试搜索无结果")
    void testSearchPosts_NoResults() throws IOException {
        // Arrange
        searchRequest.setKeyword("不存在的关键词xyz123");
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Collections.emptyList(), 0L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getRecords()).isEmpty();
        assertThat(result.getTotal()).isEqualTo(0L);
        assertThat(result.getPages()).isEqualTo(0L);

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试分页计算 - 第2页")
    void testSearchPosts_Pagination_Page2() throws IOException {
        // Arrange
        searchRequest.setPage(2);
        searchRequest.setSize(10);
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Arrays.asList(testPost), 25L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result.getCurrent()).isEqualTo(2);
        assertThat(result.getPages()).isEqualTo(3L); // 25条数据，每页10条，共3页
        assertThat(result.getHasNext()).isTrue();
        assertThat(result.getHasPrevious()).isTrue();

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试分页计算 - 最后一页")
    void testSearchPosts_Pagination_LastPage() throws IOException {
        // Arrange
        searchRequest.setPage(3);
        searchRequest.setSize(10);
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = createMockSearchResponse(Arrays.asList(testPost), 25L);
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        SearchResponse result = searchService.searchPosts(searchRequest);

        // Assert
        assertThat(result.getCurrent()).isEqualTo(3);
        assertThat(result.getPages()).isEqualTo(3L);
        assertThat(result.getHasNext()).isFalse();
        assertThat(result.getHasPrevious()).isTrue();

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试搜索异常处理 - IOException")
    void testSearchPosts_ThrowsIOException() throws IOException {
        // Arrange
        when(elasticsearchClient.search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class)))
                .thenThrow(new IOException("Elasticsearch连接失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.searchPosts(searchRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("搜索服务暂时不可用");

        verify(elasticsearchClient, times(1)).search(any(co.elastic.clients.elasticsearch.core.SearchRequest.class), eq(PostDocument.class));
    }

    // ==================== 索引文档操作测试 ====================

    @Test
    @DisplayName("测试索引帖子 - 成功")
    void testIndexPost_Success() throws IOException {
        // Arrange
        IndexResponse mockResponse = mock(IndexResponse.class);
        when(mockResponse.result()).thenReturn(Result.Created);
        when(elasticsearchClient.index(any(IndexRequest.class))).thenReturn(mockResponse);

        // Act
        assertThatCode(() -> searchService.indexPost(testPost))
                .doesNotThrowAnyException();

        // Verify
        verify(elasticsearchClient, times(1)).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("测试索引帖子 - 失败抛出异常")
    void testIndexPost_ThrowsException() throws IOException {
        // Arrange
        when(elasticsearchClient.index(any(IndexRequest.class)))
                .thenThrow(new IOException("索引失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.indexPost(testPost))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("索引帖子失败");

        verify(elasticsearchClient, times(1)).index(any(IndexRequest.class));
    }

    @Test
    @DisplayName("测试更新帖子索引 - 成功")
    void testUpdatePostIndex_Success() throws IOException {
        // Arrange
        UpdateResponse<PostDocument> mockResponse = mock(UpdateResponse.class);
        when(mockResponse.result()).thenReturn(Result.Updated);
        when(elasticsearchClient.update(any(UpdateRequest.class), eq(PostDocument.class)))
                .thenReturn(mockResponse);

        // Act
        assertThatCode(() -> searchService.updatePostIndex(testPost))
                .doesNotThrowAnyException();

        // Verify
        verify(elasticsearchClient, times(1)).update(any(UpdateRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试更新帖子索引 - 失败抛出异常")
    void testUpdatePostIndex_ThrowsException() throws IOException {
        // Arrange
        when(elasticsearchClient.update(any(UpdateRequest.class), eq(PostDocument.class)))
                .thenThrow(new IOException("更新失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.updatePostIndex(testPost))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("更新帖子索引失败");

        verify(elasticsearchClient, times(1)).update(any(UpdateRequest.class), eq(PostDocument.class));
    }

    @Test
    @DisplayName("测试删除帖子索引 - 成功")
    void testDeletePostIndex_Success() throws IOException {
        // Arrange
        DeleteResponse mockResponse = mock(DeleteResponse.class);
        when(mockResponse.result()).thenReturn(Result.Deleted);
        when(elasticsearchClient.delete(any(DeleteRequest.class)))
                .thenReturn(mockResponse);

        // Act
        assertThatCode(() -> searchService.deletePostIndex(1L))
                .doesNotThrowAnyException();

        // Verify
        verify(elasticsearchClient, times(1)).delete(any(DeleteRequest.class));
    }

    @Test
    @DisplayName("测试删除帖子索引 - 失败抛出异常")
    void testDeletePostIndex_ThrowsException() throws IOException {
        // Arrange
        when(elasticsearchClient.delete(any(DeleteRequest.class)))
                .thenThrow(new IOException("删除失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.deletePostIndex(1L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("删除帖子索引失败");

        verify(elasticsearchClient, times(1)).delete(any(DeleteRequest.class));
    }

    // ==================== 索引管理测试 ====================

    @Test
    @DisplayName("测试检查索引是否存在 - 存在")
    void testIndexExists_ReturnsTrue() throws IOException {
        // Arrange
        BooleanResponse mockResponse = mock(BooleanResponse.class);
        when(mockResponse.value()).thenReturn(true);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(mockResponse);

        // Act
        boolean exists = searchService.indexExists();

        // Assert
        assertThat(exists).isTrue();
        verify(indicesClient, times(1)).exists(any(ExistsRequest.class));
    }

    @Test
    @DisplayName("测试检查索引是否存在 - 不存在")
    void testIndexExists_ReturnsFalse() throws IOException {
        // Arrange
        BooleanResponse mockResponse = mock(BooleanResponse.class);
        when(mockResponse.value()).thenReturn(false);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(mockResponse);

        // Act
        boolean exists = searchService.indexExists();

        // Assert
        assertThat(exists).isFalse();
        verify(indicesClient, times(1)).exists(any(ExistsRequest.class));
    }

    @Test
    @DisplayName("测试检查索引是否存在 - 异常返回false")
    void testIndexExists_ThrowsException_ReturnsFalse() throws IOException {
        // Arrange
        when(indicesClient.exists(any(ExistsRequest.class)))
                .thenThrow(new IOException("连接失败"));

        // Act
        boolean exists = searchService.indexExists();

        // Assert
        assertThat(exists).isFalse();
        verify(indicesClient, times(1)).exists(any(ExistsRequest.class));
    }

    @Test
    @DisplayName("测试创建索引 - 成功")
    void testCreateIndex_Success() throws IOException {
        // Arrange
        CreateIndexResponse mockResponse = mock(CreateIndexResponse.class);
        when(mockResponse.acknowledged()).thenReturn(true);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(mockResponse);

        // Act
        assertThatCode(() -> searchService.createIndex())
                .doesNotThrowAnyException();

        // Verify
        verify(indicesClient, times(1)).create(any(CreateIndexRequest.class));
    }

    @Test
    @DisplayName("测试创建索引 - 失败抛出异常")
    void testCreateIndex_ThrowsException() throws IOException {
        // Arrange
        when(indicesClient.create(any(CreateIndexRequest.class)))
                .thenThrow(new IOException("创建索引失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.createIndex())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("创建索引失败");

        verify(indicesClient, times(1)).create(any(CreateIndexRequest.class));
    }

    @Test
    @DisplayName("测试删除索引 - 成功")
    void testDeleteIndex_Success() throws IOException {
        // Arrange
        DeleteIndexResponse mockResponse = mock(DeleteIndexResponse.class);
        when(mockResponse.acknowledged()).thenReturn(true);
        when(indicesClient.delete(any(DeleteIndexRequest.class))).thenReturn(mockResponse);

        // Act
        assertThatCode(() -> searchService.deleteIndex())
                .doesNotThrowAnyException();

        // Verify
        verify(indicesClient, times(1)).delete(any(DeleteIndexRequest.class));
    }

    @Test
    @DisplayName("测试删除索引 - 失败抛出异常")
    void testDeleteIndex_ThrowsException() throws IOException {
        // Arrange
        when(indicesClient.delete(any(DeleteIndexRequest.class)))
                .thenThrow(new IOException("删除索引失败"));

        // Act & Assert
        assertThatThrownBy(() -> searchService.deleteIndex())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("删除索引失败");

        verify(indicesClient, times(1)).delete(any(DeleteIndexRequest.class));
    }

    @Test
    @DisplayName("测试重建索引 - 索引已存在")
    void testRebuildAllIndexes_IndexExists() throws IOException {
        // Arrange
        BooleanResponse existsResponse = mock(BooleanResponse.class);
        when(existsResponse.value()).thenReturn(true);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);

        DeleteIndexResponse deleteResponse = mock(DeleteIndexResponse.class);
        when(deleteResponse.acknowledged()).thenReturn(true);
        when(indicesClient.delete(any(DeleteIndexRequest.class))).thenReturn(deleteResponse);

        CreateIndexResponse createResponse = mock(CreateIndexResponse.class);
        when(createResponse.acknowledged()).thenReturn(true);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createResponse);

        // Act
        assertThatCode(() -> searchService.rebuildAllIndexes())
                .doesNotThrowAnyException();

        // Verify
        verify(indicesClient, times(1)).exists(any(ExistsRequest.class));
        verify(indicesClient, times(1)).delete(any(DeleteIndexRequest.class));
        verify(indicesClient, times(1)).create(any(CreateIndexRequest.class));
    }

    @Test
    @DisplayName("测试重建索引 - 索引不存在")
    void testRebuildAllIndexes_IndexNotExists() throws IOException {
        // Arrange
        BooleanResponse existsResponse = mock(BooleanResponse.class);
        when(existsResponse.value()).thenReturn(false);
        when(indicesClient.exists(any(ExistsRequest.class))).thenReturn(existsResponse);

        CreateIndexResponse createResponse = mock(CreateIndexResponse.class);
        when(createResponse.acknowledged()).thenReturn(true);
        when(indicesClient.create(any(CreateIndexRequest.class))).thenReturn(createResponse);

        // Act
        assertThatCode(() -> searchService.rebuildAllIndexes())
                .doesNotThrowAnyException();

        // Verify
        verify(indicesClient, times(1)).exists(any(ExistsRequest.class));
        verify(indicesClient, never()).delete(any(DeleteIndexRequest.class));
        verify(indicesClient, times(1)).create(any(CreateIndexRequest.class));
    }

    // ==================== 搜索建议测试 ====================

    @Test
    @DisplayName("测试获取搜索建议 - 成功")
    void testGetSearchSuggestions_Success() {
        // Act
        List<String> suggestions = searchService.getSearchSuggestions("Java", 4);

        // Assert
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(4);
        assertThat(suggestions).containsExactly(
                "Java教程",
                "Java经验分享",
                "Java问题解决",
                "Java最佳实践"
        );
    }

    @Test
    @DisplayName("测试获取搜索建议 - 限制数量")
    void testGetSearchSuggestions_LimitSize() {
        // Act
        List<String> suggestions = searchService.getSearchSuggestions("Spring", 2);

        // Assert
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(2);
        assertThat(suggestions.get(0)).isEqualTo("Spring教程");
        assertThat(suggestions.get(1)).isEqualTo("Spring经验分享");
    }

    @Test
    @DisplayName("测试获取搜索建议 - 最大数量")
    void testGetSearchSuggestions_MaxSize() {
        // Act
        List<String> suggestions = searchService.getSearchSuggestions("Python", 10);

        // Assert
        assertThat(suggestions).isNotNull();
        assertThat(suggestions).hasSize(4); // 最多返回4个
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建模拟的搜索响应
     */
    @SuppressWarnings("unchecked")
    private co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> createMockSearchResponse(List<PostDocument> documents, long total) {
        co.elastic.clients.elasticsearch.core.SearchResponse<PostDocument> mockResponse = mock(co.elastic.clients.elasticsearch.core.SearchResponse.class);
        HitsMetadata<PostDocument> hitsMetadata = mock(HitsMetadata.class);

        List<Hit<PostDocument>> hits = documents.stream()
                .map(doc -> {
                    Hit<PostDocument> hit = mock(Hit.class);
                    when(hit.source()).thenReturn(doc);
                    return hit;
                })
                .toList();

        TotalHits totalHits = mock(TotalHits.class);
        when(totalHits.value()).thenReturn(total);
        when(totalHits.relation()).thenReturn(TotalHitsRelation.Eq);

        when(hitsMetadata.hits()).thenReturn(hits);
        when(hitsMetadata.total()).thenReturn(totalHits);
        when(mockResponse.hits()).thenReturn(hitsMetadata);

        return mockResponse;
    }
}
