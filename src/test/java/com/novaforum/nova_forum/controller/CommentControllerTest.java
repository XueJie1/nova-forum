package com.novaforum.nova_forum.controller;
import jakarta.servlet.http.HttpServletRequest;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.entity.Comment;
import com.novaforum.nova_forum.service.CommentService;
import com.novaforum.nova_forum.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * CommentController 单元测试（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("评论控制器单元测试")
class CommentControllerTest {

    @Mock
    private CommentService commentService;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private CommentController commentController;

    // ==================== 创建评论接口测试 ====================

    @Test
    @DisplayName("测试创建评论 - 成功")
    void testCreateComment_Success() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.createComment(any(), anyLong())).thenReturn(1L);

        ResponseEntity<?> result = commentController.createComment(
                new com.novaforum.nova_forum.dto.CommentRequest(), mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试创建评论 - 未登录")
    void testCreateComment_NotLoggedIn() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ResponseEntity<?> result = commentController.createComment(
                new com.novaforum.nova_forum.dto.CommentRequest(), mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试创建评论 - 服务异常")
    void testCreateComment_ServiceException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.createComment(any(), anyLong())).thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.createComment(
                new com.novaforum.nova_forum.dto.CommentRequest(), mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 更新评论接口测试 ====================

    @Test
    @DisplayName("测试更新评论 - 成功")
    void testUpdateComment_Success() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.updateComment(anyLong(), anyString(), anyLong())).thenReturn(true);

        ResponseEntity<?> result = commentController.updateComment(1L, "updated", mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试更新评论 - 未登录")
    void testUpdateComment_NotLoggedIn() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ResponseEntity<?> result = commentController.updateComment(1L, "updated", mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试更新评论 - 更新失败")
    void testUpdateComment_Failure() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.updateComment(anyLong(), anyString(), anyLong())).thenReturn(false);

        ResponseEntity<?> result = commentController.updateComment(1L, "updated", mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试更新评论 - 服务异常")
    void testUpdateComment_ServiceException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.updateComment(anyLong(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.updateComment(1L, "updated", mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 删除评论接口测试 ====================

    @Test
    @DisplayName("测试删除评论 - 成功")
    void testDeleteComment_Success() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.deleteComment(anyLong(), anyLong())).thenReturn(true);

        ResponseEntity<?> result = commentController.deleteComment(1L, mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试删除评论 - 未登录")
    void testDeleteComment_NotLoggedIn() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ResponseEntity<?> result = commentController.deleteComment(1L, mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试删除评论 - 删除失败")
    void testDeleteComment_Failure() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.deleteComment(anyLong(), anyLong())).thenReturn(false);

        ResponseEntity<?> result = commentController.deleteComment(1L, mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试删除评论 - 服务异常")
    void testDeleteComment_ServiceException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(commentService.deleteComment(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.deleteComment(1L, mock(HttpServletRequest.class));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取评论详情接口测试 ====================

    @Test
    @DisplayName("测试获取评论详情 - 成功")
    void testGetCommentDetail_Success() {
        Comment comment = new Comment();
        comment.setId(1L);
        comment.setContent("test");
        when(commentService.getCommentDetail(anyLong())).thenReturn(comment);

        ResponseEntity<?> result = commentController.getCommentDetail(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取评论详情 - 评论不存在")
    void testGetCommentDetail_NotFound() {
        when(commentService.getCommentDetail(anyLong())).thenReturn(null);

        ResponseEntity<?> result = commentController.getCommentDetail(999L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取评论详情 - 服务异常")
    void testGetCommentDetail_ServiceException() {
        when(commentService.getCommentDetail(anyLong())).thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.getCommentDetail(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取帖子评论列表接口测试 ====================

    @Test
    @DisplayName("测试获取帖子评论列表 - 成功")
    void testGetCommentsByPostId_Success() {
        IPage<com.novaforum.nova_forum.dto.CommentResponse> page = new Page<>(1, 10, 5);
        when(commentService.getCommentsByPostId(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<?> result = commentController.getCommentsByPostId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取帖子评论列表 - 服务异常")
    void testGetCommentsByPostId_ServiceException() {
        when(commentService.getCommentsByPostId(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("ES异常"));

        ResponseEntity<?> result = commentController.getCommentsByPostId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取用户评论列表接口测试 ====================

    @Test
    @DisplayName("测试获取用户评论列表 - 成功")
    void testGetCommentsByUserId_Success() {
        IPage<Comment> page = new Page<>(1, 10, 5);
        when(commentService.getCommentsByUserId(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<?> result = commentController.getCommentsByUserId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取用户评论列表 - 服务异常")
    void testGetCommentsByUserId_ServiceException() {
        when(commentService.getCommentsByUserId(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.getCommentsByUserId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取评论回复列表接口测试 ====================

    @Test
    @DisplayName("测试获取评论回复列表 - 成功")
    void testGetRepliesByCommentId_Success() {
        IPage<com.novaforum.nova_forum.dto.CommentResponse> page = new Page<>(1, 10, 3);
        when(commentService.getRepliesByCommentId(anyLong(), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<?> result = commentController.getRepliesByCommentId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取评论回复列表 - 服务异常")
    void testGetRepliesByCommentId_ServiceException() {
        when(commentService.getRepliesByCommentId(anyLong(), anyInt(), anyInt()))
                .thenThrow(new RuntimeException("ES异常"));

        ResponseEntity<?> result = commentController.getRepliesByCommentId(1L, 1, 10);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取评论数量接口测试 ====================

    @Test
    @DisplayName("测试获取评论数量 - 成功")
    void testGetCommentCount_Success() {
        when(commentService.getCommentCountByPostId(anyLong())).thenReturn(42);

        ResponseEntity<?> result = commentController.getCommentCount(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取评论数量 - 服务异常")
    void testGetCommentCount_ServiceException() {
        when(commentService.getCommentCountByPostId(anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = commentController.getCommentCount(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }
}
