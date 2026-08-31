package com.novaforum.nova_forum.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.mapper.PostMapper;
import com.novaforum.nova_forum.service.PostService;
import com.novaforum.nova_forum.service.PostSyncService;
import com.novaforum.nova_forum.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * PostServiceImpl 单元测试
 *
 * 测试覆盖：
 * - createPost 参数校验 / 成功 / 插入失败 / ES 同步失败
 * - updatePost 校验 / 帖子不存在 / 非作者 / 成功
 * - deletePost 校验 / 帖子不存在 / 非作者 / 成功
 * - getPostDetail 空ID / 不存在 / 成功（浏览数自增）
 * - getPostList 分页默认值
 * - incrementViewCount / incrementLikeCount / decrementLikeCount
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("帖子服务单元测试")
class PostServiceImplTest {

    @Mock
    private PostMapper postMapper;

    @Mock
    private UserService userService;

    @Mock
    private PostSyncService postSyncService;

    @InjectMocks
    private PostServiceImpl postService;

    private Post testPost;

    @BeforeEach
    void setUp() {
        testPost = new Post();
        testPost.setId(1L);
        testPost.setTitle("标题");
        testPost.setContent("内容");
    }

    // ==================== createPost ====================

    @Test
    @DisplayName("createPost - 空参数抛出IllegalArgumentException")
    void testCreatePost_EmptyParams_Throws() {
        assertThatIllegalArgumentException().isThrownBy(() -> postService.createPost(null, 1L));
        assertThatIllegalArgumentException().isThrownBy(() -> postService.createPost(testPost, null));

        Post empty = new Post();
        assertThatIllegalArgumentException().isThrownBy(() -> postService.createPost(empty, 1L));
        verifyNoInteractions(postMapper);
    }

    @Test
    @DisplayName("createPost - 成功创建并同步ES")
    void testCreatePost_Success() {
        when(postMapper.insert(any(Post.class))).thenReturn(1);

        Long id = postService.createPost(testPost, 100L);

        assertThat(id).isEqualTo(1L);
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).insert(captor.capture());
        Post saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(100L);
        assertThat(saved.getViewCount()).isEqualTo(0);
        assertThat(saved.getLikeCount()).isEqualTo(0);
        assertThat(saved.getCreateTime()).isNotNull();
        assertThat(saved.getUpdateTime()).isNotNull();
        verify(postSyncService).syncPostToElasticsearch(any(Post.class));
    }

    @Test
    @DisplayName("createPost - 插入失败抛出异常")
    void testCreatePost_InsertFails_Throws() {
        when(postMapper.insert(any(Post.class))).thenReturn(0);
        assertThatRuntimeException().isThrownBy(() -> postService.createPost(testPost, 100L))
                .withMessageContaining("创建帖子失败");
    }

    @Test
    @DisplayName("createPost - ES同步失败不影响创建")
    void testCreatePost_EsSyncFails_DoesNotThrow() {
        when(postMapper.insert(any(Post.class))).thenReturn(1);
        doThrow(new RuntimeException("ES down")).when(postSyncService).syncPostToElasticsearch(any(Post.class));

        Long id = postService.createPost(testPost, 100L);

        assertThat(id).isEqualTo(1L);
    }

    // ==================== updatePost ====================

    @Test
    @DisplayName("updatePost - 空参数抛出IllegalArgumentException")
    void testUpdatePost_EmptyParams_Throws() {
        assertThatIllegalArgumentException().isThrownBy(() -> postService.updatePost(null, 1L));
        Post empty = new Post();
        assertThatIllegalArgumentException().isThrownBy(() -> postService.updatePost(empty, 1L));
    }

    @Test
    @DisplayName("updatePost - 帖子不存在时抛出IllegalArgumentException")
    void testUpdatePost_NotFound_ThrowsIllegalArgumentException() {
        when(postMapper.selectById(1L)).thenReturn(null);
        testPost.setId(1L);
        assertThatThrownBy(() -> postService.updatePost(testPost, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("帖子不存在");
    }

    @Test
    @DisplayName("updatePost - 非作者无法编辑，抛出SecurityException")
    void testUpdatePost_NotAuthor_ThrowsSecurityException() {
        Post existing = new Post();
        existing.setId(1L);
        existing.setUserId(999L);
        when(postMapper.selectById(1L)).thenReturn(existing);
        testPost.setId(1L);
        assertThatThrownBy(() -> postService.updatePost(testPost, 100L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只能编辑自己的帖子");
    }

    @Test
    @DisplayName("updatePost - 成功更新并保持原浏览/点赞数")
    void testUpdatePost_Success() {
        Post existing = new Post();
        existing.setId(1L);
        existing.setUserId(100L);
        existing.setViewCount(500);
        existing.setLikeCount(25);
        when(postMapper.selectById(1L)).thenReturn(existing);
        when(postMapper.updateById(any(Post.class))).thenReturn(1);

        boolean result = postService.updatePost(testPost, 100L);

        assertThat(result).isTrue();
        ArgumentCaptor<Post> captor = ArgumentCaptor.forClass(Post.class);
        verify(postMapper).updateById(captor.capture());
        Post updated = captor.getValue();
        assertThat(updated.getUserId()).isEqualTo(100L);
        assertThat(updated.getViewCount()).isEqualTo(500);
        assertThat(updated.getLikeCount()).isEqualTo(25);
    }

    // ==================== deletePost ====================

    @Test
    @DisplayName("deletePost - 空参数抛出IllegalArgumentException")
    void testDeletePost_EmptyParams_Throws() {
        assertThatIllegalArgumentException().isThrownBy(() -> postService.deletePost(null, null));
    }

    @Test
    @DisplayName("deletePost - 帖子不存在时抛出IllegalArgumentException")
    void testDeletePost_NotFound_ThrowsIllegalArgumentException() {
        when(postMapper.selectById(1L)).thenReturn(null);
        assertThatThrownBy(() -> postService.deletePost(1L, 100L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("帖子不存在");
    }

    @Test
    @DisplayName("deletePost - 非作者无法删除，抛出SecurityException")
    void testDeletePost_NotAuthor_ThrowsSecurityException() {
        Post existing = new Post();
        existing.setId(1L);
        existing.setUserId(999L);
        when(postMapper.selectById(1L)).thenReturn(existing);
        assertThatThrownBy(() -> postService.deletePost(1L, 100L))
                .isInstanceOf(SecurityException.class)
                .hasMessageContaining("只能删除自己的帖子");
    }

    @Test
    @DisplayName("deletePost - 成功删除并删除ES索引")
    void testDeletePost_Success() {
        Post existing = new Post();
        existing.setId(1L);
        existing.setUserId(100L);
        when(postMapper.selectById(1L)).thenReturn(existing);
        when(postMapper.deleteById(1L)).thenReturn(1);

        boolean result = postService.deletePost(1L, 100L);

        assertThat(result).isTrue();
        verify(postMapper).deleteById(1L);
        verify(postSyncService).deletePostFromElasticsearch(1L);
    }

    // ==================== getPostDetail ====================

    @Test
    @DisplayName("getPostDetail - 空ID抛出IllegalArgumentException")
    void testGetPostDetail_NullId_Throws() {
        assertThatIllegalArgumentException().isThrownBy(() -> postService.getPostDetail(null));
        verifyNoInteractions(postMapper);
    }

    @Test
    @DisplayName("getPostDetail - 帖子不存在时抛出IllegalArgumentException")
    void testGetPostDetail_NotFound_ThrowsIllegalArgumentException() {
        when(postMapper.selectPostWithAuthorById(1L)).thenReturn(null);
        assertThatThrownBy(() -> postService.getPostDetail(1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("帖子不存在");
    }

    @Test
    @DisplayName("getPostDetail - 成功并增加浏览数")
    void testGetPostDetail_Success_IncrementsViewCount() {
        when(postMapper.selectPostWithAuthorById(1L)).thenReturn(testPost);
        when(postMapper.incrementViewCount(1L)).thenReturn(1);

        Post result = postService.getPostDetail(1L);

        assertThat(result).isSameAs(testPost);
        verify(postMapper).incrementViewCount(1L);
    }

    // ==================== getPostList ====================

    @Test
    @DisplayName("getPostList - 默认分页参数")
    void testGetPostList_DefaultPagination() {
        Page<Post> page = new Page<>(1, 10);
        when(postMapper.selectPostsPage(any(Page.class), any())).thenReturn(page);

        postService.getPostList(null, null, null);

        ArgumentCaptor<Page> captor = ArgumentCaptor.forClass(Page.class);
        verify(postMapper).selectPostsPage(captor.capture(), any());
        Page<Post> captured = captor.getValue();
        assertThat(captured.getCurrent()).isEqualTo(1);
        assertThat(captured.getSize()).isEqualTo(10);
    }

    @Test
    @DisplayName("getPostList - 页码小于1默认为1")
    void testGetPostList_PageNumBelow1_DefaultsToOne() {
        Page<Post> page = new Page<>(1, 10);
        when(postMapper.selectPostsPage(any(Page.class), any())).thenReturn(page);

        postService.getPostList(0, 3, null);

        ArgumentCaptor<Page> captor = ArgumentCaptor.forClass(Page.class);
        verify(postMapper).selectPostsPage(captor.capture(), any());
        assertThat(captor.getValue().getCurrent()).isEqualTo(1);
    }

    @Test
    @DisplayName("getPostList - 每页超出范围回退为默认10")
    void testGetPostList_PageSizeOutOfRange_FallsBackToDefault() {
        Page<Post> page = new Page<>(1, 10);
        when(postMapper.selectPostsPage(any(Page.class), any())).thenReturn(page);

        postService.getPostList(1, 500, null);

        ArgumentCaptor<Page> captor = ArgumentCaptor.forClass(Page.class);
        verify(postMapper).selectPostsPage(captor.capture(), any());
        assertThat(captor.getValue().getSize()).isEqualTo(10);
    }

    // ==================== 计数自增方法 ====================

    @Test
    @DisplayName("incrementViewCount - 空ID直接返回")
    void testIncrementViewCount_NullId_NoOp() {
        postService.incrementViewCount(null);
        verifyNoInteractions(postMapper);
    }

    @Test
    @DisplayName("incrementViewCount - 异常被吞掉")
    void testIncrementViewCount_ExceptionSwallowed() {
        when(postMapper.incrementViewCount(1L)).thenThrow(new RuntimeException("db down"));
        // 不应抛出异常
        assertThatCode(() -> postService.incrementViewCount(1L)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("incrementLikeCount / decrementLikeCount - 正常调用")
    void testLikeCountIncrements() {
        postService.incrementLikeCount(1L);
        postService.decrementLikeCount(1L);
        verify(postMapper).incrementLikeCount(1L);
        verify(postMapper).decrementLikeCount(1L);
    }

    @Test
    @DisplayName("incrementLikeCount - 空ID直接返回")
    void testIncrementLikeCount_NullId_NoOp() {
        postService.incrementLikeCount(null);
        verifyNoInteractions(postMapper);
    }
}
