package com.novaforum.nova_forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.entity.Post;
import com.novaforum.nova_forum.entity.User;
import com.novaforum.nova_forum.service.PostService;
import com.novaforum.nova_forum.service.UserService;
import com.novaforum.nova_forum.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * PostController 单元测试（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("帖子控制器单元测试")
class PostControllerTest {

    @Mock
    private PostService postService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserService userService;

    @InjectMocks
    private PostController postController;

    // ==================== 发布帖子接口测试 ====================

    @Test
    @DisplayName("测试发布帖子 - 成功")
    void testCreatePost_Success() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("test title");
        request.setContent("test content");

        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.createPost(any(Post.class), anyLong())).thenReturn(1L);

        ApiResponse<String> result = postController.createPost(request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(200);
        assertThat(result.getMessage()).isEqualTo("帖子发布成功");
    }

    @Test
    @DisplayName("测试发布帖子 - 无效授权头")
    void testCreatePost_InvalidAuthHeader() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("test");
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        ApiResponse<String> result = postController.createPost(request, "Bearer invalid");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试发布帖子 - Token无法提取用户ID")
    void testCreatePost_CannotExtractUserId() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("test");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ApiResponse<String> result = postController.createPost(request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试发布帖子 - 参数异常")
    void testCreatePost_IllegalArgumentException() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("test");
        request.setContent("content");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.createPost(any(Post.class), anyLong()))
                .thenThrow(new IllegalArgumentException("标题不能为空"));

        ApiResponse<String> result = postController.createPost(request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试发布帖子 - 服务异常")
    void testCreatePost_ServiceException() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("test");
        request.setContent("content");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.createPost(any(Post.class), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ApiResponse<String> result = postController.createPost(request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 更新帖子接口测试 ====================

    @Test
    @DisplayName("测试更新帖子 - 成功")
    void testUpdatePost_Success() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("updated");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.updatePost(any(Post.class), anyLong())).thenReturn(true);

        ApiResponse<String> result = postController.updatePost(1L, request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试更新帖子 - 无效Token")
    void testUpdatePost_InvalidToken() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("updated");
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        ApiResponse<String> result = postController.updatePost(1L, request, "Bearer invalid");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试更新帖子 - 更新失败")
    void testUpdatePost_Failure() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("updated");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.updatePost(any(Post.class), anyLong())).thenReturn(false);

        ApiResponse<String> result = postController.updatePost(1L, request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试更新帖子 - 权限异常")
    void testUpdatePost_SecurityException() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("updated");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.updatePost(any(Post.class), anyLong()))
                .thenThrow(new SecurityException("无权限操作"));

        ApiResponse<String> result = postController.updatePost(1L, request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(403);
    }

    @Test
    @DisplayName("测试更新帖子 - 服务异常")
    void testUpdatePost_ServiceException() {
        com.novaforum.nova_forum.dto.PostRequest request = new com.novaforum.nova_forum.dto.PostRequest();
        request.setTitle("updated");
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.updatePost(any(Post.class), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ApiResponse<String> result = postController.updatePost(1L, request, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 删除帖子接口测试 ====================

    @Test
    @DisplayName("测试删除帖子 - 成功")
    void testDeletePost_Success() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.deletePost(anyLong(), anyLong())).thenReturn(true);

        ApiResponse<String> result = postController.deletePost(1L, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试删除帖子 - 无效Token")
    void testDeletePost_InvalidToken() {
        when(jwtUtil.validateToken(anyString())).thenReturn(false);

        ApiResponse<String> result = postController.deletePost(1L, "Bearer invalid");

        assertThat(result.getCode()).isEqualTo(401);
    }

    @Test
    @DisplayName("测试删除帖子 - 删除失败")
    void testDeletePost_Failure() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.deletePost(anyLong(), anyLong())).thenReturn(false);

        ApiResponse<String> result = postController.deletePost(1L, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试删除帖子 - 服务异常")
    void testDeletePost_ServiceException() {
        when(jwtUtil.validateToken(anyString())).thenReturn(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(postService.deletePost(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ApiResponse<String> result = postController.deletePost(1L, "Bearer test-token");

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 获取帖子详情接口测试 ====================

    @Test
    @DisplayName("测试获取帖子详情 - 成功")
    void testGetPostDetail_Success() {
        Post post = new Post();
        post.setId(1L);
        post.setTitle("test title");
        post.setUserId(100L);
        when(postService.getPostDetail(anyLong())).thenReturn(post);
        User user = new User();
        user.setId(100L);
        user.setUsername("testuser");
        when(userService.findById(anyLong())).thenReturn(user);

        @SuppressWarnings("unchecked")
        ApiResponse<com.novaforum.nova_forum.dto.PostResponse> result = postController.getPostDetail(1L);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取帖子详情 - 参数异常")
    void testGetPostDetail_IllegalArgumentException() {
        when(postService.getPostDetail(anyLong())).thenThrow(new IllegalArgumentException("帖子不存在"));

        @SuppressWarnings("unchecked")
        ApiResponse<com.novaforum.nova_forum.dto.PostResponse> result = postController.getPostDetail(999L);

        assertThat(result.getCode()).isEqualTo(400);
    }

    @Test
    @DisplayName("测试获取帖子详情 - 服务异常")
    void testGetPostDetail_ServiceException() {
        when(postService.getPostDetail(anyLong())).thenThrow(new RuntimeException("数据库异常"));

        @SuppressWarnings("unchecked")
        ApiResponse<com.novaforum.nova_forum.dto.PostResponse> result = postController.getPostDetail(1L);

        assertThat(result.getCode()).isEqualTo(500);
    }

    // ==================== 获取帖子列表接口测试 ====================

    @Test
    @DisplayName("测试获取帖子列表 - 成功")
    void testGetPostList_Success() {
        IPage<Post> page = new Page<>(1, 10, 5);
        Post post = new Post();
        post.setId(1L);
        post.setTitle("test");
        post.setUserId(100L);
        page.setRecords(Arrays.asList(post));
        User user = new User();
        user.setUsername("testuser");
        when(userService.findById(anyLong())).thenReturn(user);
        when(postService.getPostList(anyInt(), anyInt(), any())).thenReturn(page);

        @SuppressWarnings("unchecked")
        ApiResponse<IPage<com.novaforum.nova_forum.dto.PostResponse>> result = postController.getPostList(1, 10, null);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取帖子列表 - 带用户筛选")
    void testGetPostList_WithUserId() {
        IPage<Post> page = new Page<>(1, 10, 5);
        when(postService.getPostList(anyInt(), anyInt(), any())).thenReturn(page);

        @SuppressWarnings("unchecked")
        ApiResponse<IPage<com.novaforum.nova_forum.dto.PostResponse>> result = postController.getPostList(1, 10, 100L);

        assertThat(result.getCode()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取帖子列表 - 服务异常")
    void testGetPostList_ServiceException() {
        when(postService.getPostList(anyInt(), anyInt(), any()))
                .thenThrow(new RuntimeException("数据库异常"));

        @SuppressWarnings("unchecked")
        ApiResponse<IPage<com.novaforum.nova_forum.dto.PostResponse>> result = postController.getPostList(1, 10, null);

        assertThat(result.getCode()).isEqualTo(500);
    }
}
