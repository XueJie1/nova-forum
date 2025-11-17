package com.novaforum.nova_forum.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.PostDocument;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.SearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * PostSyncServiceImpl 单元测试
 *
 * 测试覆盖：
 * - 单个帖子同步到 Elasticsearch
 * - 从 Elasticsearch 删除帖子
 * - 批量同步所有帖子
 * - 增量同步（最近N分钟）
 * - 异常处理
 * - Post 到 PostDocument 的转换
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("帖子同步服务单元测试")
class PostSyncServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private SearchService searchService;

    @InjectMocks
    private PostSyncServiceImpl postSyncService;

    private Post testPost;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();

        testPost = new Post();
        testPost.setId(1L);
        testPost.setUserId(100L);
        testPost.setTitle("Java编程基础教程");
        testPost.setContent("这是一篇关于Java编程的基础教程");
        testPost.setViewCount(1000);
        testPost.setLikeCount(50);
        testPost.setCreateTime(now);
        testPost.setUpdateTime(now);
    }

    // ==================== 单个帖子同步测试 ====================

    @Test
    @DisplayName("测试同步帖子到ES - 索引已存在")
    void testSyncPostToElasticsearch_IndexExists_Success() {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncPostToElasticsearch(testPost);

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(searchService, never()).createIndex(); // 索引已存在，不创建
        verify(searchService, times(1)).indexPost(any(PostDocument.class));

        // 验证转换后的PostDocument
        ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);
        verify(searchService).indexPost(captor.capture());
        PostDocument captured = captor.getValue();

        assertThat(captured.getId()).isEqualTo(1L);
        assertThat(captured.getTitle()).isEqualTo("Java编程基础教程");
        assertThat(captured.getContent()).isEqualTo("这是一篇关于Java编程的基础教程");
        assertThat(captured.getUserId()).isEqualTo(100L);
        assertThat(captured.getUsername()).isEqualTo("用户100");
        assertThat(captured.getViewCount()).isEqualTo(1000);
        assertThat(captured.getLikeCount()).isEqualTo(50);
        assertThat(captured.getCreateTime()).isEqualTo(now);
        assertThat(captured.getUpdateTime()).isEqualTo(now);
    }

    @Test
    @DisplayName("测试同步帖子到ES - 索引不存在，自动创建")
    void testSyncPostToElasticsearch_IndexNotExists_CreateIndex() {
        // Arrange
        when(searchService.indexExists()).thenReturn(false);
        doNothing().when(searchService).createIndex();
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncPostToElasticsearch(testPost);

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(searchService, times(1)).createIndex(); // 索引不存在，创建
        verify(searchService, times(1)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试同步帖子到ES - 同步失败不抛出异常")
    void testSyncPostToElasticsearch_FailsGracefully() {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        doThrow(new RuntimeException("ES连接失败")).when(searchService).indexPost(any(PostDocument.class));

        // Act - 不应该抛出异常
        assertThatCode(() -> postSyncService.syncPostToElasticsearch(testPost))
                .doesNotThrowAnyException();

        // Assert
        verify(searchService, times(1)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试同步帖子到ES - 检查索引存在时失败")
    void testSyncPostToElasticsearch_IndexExistsCheckFails() {
        // Arrange
        when(searchService.indexExists()).thenThrow(new RuntimeException("ES连接失败"));

        // Act - 不应该抛出异常
        assertThatCode(() -> postSyncService.syncPostToElasticsearch(testPost))
                .doesNotThrowAnyException();

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(searchService, never()).indexPost(any(PostDocument.class));
    }

    // ==================== 删除帖子索引测试 ====================

    @Test
    @DisplayName("测试从ES删除帖子 - 成功")
    void testDeletePostFromElasticsearch_Success() {
        // Arrange
        doNothing().when(searchService).deletePostIndex(1L);

        // Act
        postSyncService.deletePostFromElasticsearch(1L);

        // Assert
        verify(searchService, times(1)).deletePostIndex(1L);
    }

    @Test
    @DisplayName("测试从ES删除帖子 - 删除失败不抛出异常")
    void testDeletePostFromElasticsearch_FailsGracefully() {
        // Arrange
        doThrow(new RuntimeException("ES连接失败")).when(searchService).deletePostIndex(anyLong());

        // Act - 不应该抛出异常
        assertThatCode(() -> postSyncService.deletePostFromElasticsearch(1L))
                .doesNotThrowAnyException();

        // Assert
        verify(searchService, times(1)).deletePostIndex(1L);
    }

    // ==================== 批量同步所有帖子测试 ====================

    @Test
    @DisplayName("测试批量同步所有帖子 - 成功")
    void testSyncAllPostsToElasticsearch_Success() {
        // Arrange
        Post post1 = createPost(1L, "帖子1");
        Post post2 = createPost(2L, "帖子2");
        Post post3 = createPost(3L, "帖子3");
        List<Post> posts = Arrays.asList(post1, post2, post3);

        when(searchService.indexExists()).thenReturn(true);
        when(postMapper.selectList(null)).thenReturn(posts);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncAllPostsToElasticsearch();

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(postMapper, times(1)).selectList(null);
        verify(searchService, times(3)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试批量同步所有帖子 - 空数据库")
    void testSyncAllPostsToElasticsearch_EmptyDatabase() {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        when(postMapper.selectList(null)).thenReturn(Collections.emptyList());

        // Act
        postSyncService.syncAllPostsToElasticsearch();

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(postMapper, times(1)).selectList(null);
        verify(searchService, never()).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试批量同步所有帖子 - 索引不存在时创建")
    void testSyncAllPostsToElasticsearch_CreateIndex() {
        // Arrange
        List<Post> posts = Arrays.asList(createPost(1L, "帖子1"));

        when(searchService.indexExists()).thenReturn(false);
        when(postMapper.selectList(null)).thenReturn(posts);
        doNothing().when(searchService).createIndex();
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncAllPostsToElasticsearch();

        // Assert
        verify(searchService, times(1)).indexExists();
        verify(searchService, times(1)).createIndex();
        verify(searchService, times(1)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试批量同步所有帖子 - 部分失败")
    void testSyncAllPostsToElasticsearch_PartialFailure() {
        // Arrange
        Post post1 = createPost(1L, "帖子1");
        Post post2 = createPost(2L, "帖子2");
        Post post3 = createPost(3L, "帖子3");
        List<Post> posts = Arrays.asList(post1, post2, post3);

        when(searchService.indexExists()).thenReturn(true);
        when(postMapper.selectList(null)).thenReturn(posts);

        // 第2个帖子索引失败
        doNothing().when(searchService).indexPost(argThat(doc -> doc.getId() == 1L));
        doThrow(new RuntimeException("索引失败")).when(searchService).indexPost(argThat(doc -> doc.getId() == 2L));
        doNothing().when(searchService).indexPost(argThat(doc -> doc.getId() == 3L));

        // Act - 部分失败不影响整体流程
        assertThatCode(() -> postSyncService.syncAllPostsToElasticsearch())
                .doesNotThrowAnyException();

        // Assert - 所有帖子都尝试索引
        verify(searchService, times(3)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试批量同步所有帖子 - 查询数据库失败抛出异常")
    void testSyncAllPostsToElasticsearch_DatabaseQueryFails() {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        when(postMapper.selectList(null)).thenThrow(new RuntimeException("数据库连接失败"));

        // Act & Assert
        assertThatThrownBy(() -> postSyncService.syncAllPostsToElasticsearch())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("批量同步失败");

        verify(searchService, never()).indexPost(any(PostDocument.class));
    }

    // ==================== 增量同步测试 ====================

    @Test
    @DisplayName("测试增量同步 - 最近30分钟")
    void testSyncRecentPostsToElasticsearch_Success() {
        // Arrange
        Post post1 = createPost(1L, "最近帖子1");
        Post post2 = createPost(2L, "最近帖子2");
        List<Post> recentPosts = Arrays.asList(post1, post2);

        when(postMapper.selectList(any(QueryWrapper.class))).thenReturn(recentPosts);
        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncRecentPostsToElasticsearch(30);

        // Assert
        ArgumentCaptor<QueryWrapper> captor = ArgumentCaptor.forClass(QueryWrapper.class);
        verify(postMapper, times(1)).selectList(captor.capture());

        // 验证查询条件（最近30分钟）
        // 注意：由于QueryWrapper的内部实现，这里只验证调用了正确的方法
        verify(searchService, times(2)).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试增量同步 - 无最近更新的帖子")
    void testSyncRecentPostsToElasticsearch_NoRecentPosts() {
        // Arrange
        when(postMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act
        postSyncService.syncRecentPostsToElasticsearch(30);

        // Assert
        verify(postMapper, times(1)).selectList(any(QueryWrapper.class));
        verify(searchService, never()).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试增量同步 - 数据库查询失败")
    void testSyncRecentPostsToElasticsearch_QueryFails() {
        // Arrange
        when(postMapper.selectList(any(QueryWrapper.class)))
                .thenThrow(new RuntimeException("数据库查询失败"));

        // Act & Assert
        assertThatThrownBy(() -> postSyncService.syncRecentPostsToElasticsearch(30))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("增量同步失败");

        verify(searchService, never()).indexPost(any(PostDocument.class));
    }

    @Test
    @DisplayName("测试增量同步 - 不同时间范围")
    void testSyncRecentPostsToElasticsearch_DifferentTimeRanges() {
        // Arrange
        when(postMapper.selectList(any(QueryWrapper.class))).thenReturn(Collections.emptyList());

        // Act - 测试不同的时间范围
        postSyncService.syncRecentPostsToElasticsearch(5);   // 5分钟
        postSyncService.syncRecentPostsToElasticsearch(60);  // 1小时
        postSyncService.syncRecentPostsToElasticsearch(1440); // 24小时

        // Assert
        verify(postMapper, times(3)).selectList(any(QueryWrapper.class));
    }

    // ==================== 数据转换测试 ====================

    @Test
    @DisplayName("测试Post到PostDocument转换 - 完整数据")
    void testPostToPostDocumentConversion_CompleteData() {
        // Arrange
        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncPostToElasticsearch(testPost);

        // Assert
        ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);
        verify(searchService).indexPost(captor.capture());
        PostDocument document = captor.getValue();

        assertThat(document.getId()).isEqualTo(testPost.getId());
        assertThat(document.getTitle()).isEqualTo(testPost.getTitle());
        assertThat(document.getContent()).isEqualTo(testPost.getContent());
        assertThat(document.getUserId()).isEqualTo(testPost.getUserId());
        assertThat(document.getUsername()).isEqualTo("用户" + testPost.getUserId());
        assertThat(document.getViewCount()).isEqualTo(testPost.getViewCount());
        assertThat(document.getLikeCount()).isEqualTo(testPost.getLikeCount());
        assertThat(document.getCreateTime()).isEqualTo(testPost.getCreateTime());
        assertThat(document.getUpdateTime()).isEqualTo(testPost.getUpdateTime());
    }

    @Test
    @DisplayName("测试Post到PostDocument转换 - 零点赞零浏览")
    void testPostToPostDocumentConversion_ZeroCounts() {
        // Arrange
        testPost.setViewCount(0);
        testPost.setLikeCount(0);

        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncPostToElasticsearch(testPost);

        // Assert
        ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);
        verify(searchService).indexPost(captor.capture());
        PostDocument document = captor.getValue();

        assertThat(document.getViewCount()).isEqualTo(0);
        assertThat(document.getLikeCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("测试Post到PostDocument转换 - 长文本内容")
    void testPostToPostDocumentConversion_LongContent() {
        // Arrange
        String longContent = "A".repeat(10000); // 10000字符的长文本
        testPost.setContent(longContent);

        when(searchService.indexExists()).thenReturn(true);
        doNothing().when(searchService).indexPost(any(PostDocument.class));

        // Act
        postSyncService.syncPostToElasticsearch(testPost);

        // Assert
        ArgumentCaptor<PostDocument> captor = ArgumentCaptor.forClass(PostDocument.class);
        verify(searchService).indexPost(captor.capture());
        PostDocument document = captor.getValue();

        assertThat(document.getContent()).hasSize(10000);
    }

    // ==================== 辅助方法 ====================

    /**
     * 创建测试帖子
     */
    private Post createPost(Long id, String title) {
        Post post = new Post();
        post.setId(id);
        post.setUserId(100L);
        post.setTitle(title);
        post.setContent("这是帖子" + id + "的内容");
        post.setViewCount(100);
        post.setLikeCount(10);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        return post;
    }
}
