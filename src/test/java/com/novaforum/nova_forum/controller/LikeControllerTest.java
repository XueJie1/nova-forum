package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.LikeResponse;
import com.novaforum.nova_forum.service.LikeService;
import com.novaforum.nova_forum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * LikeController 单元测试（纯单元测试）
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("点赞控制器单元测试")
class LikeControllerTest {

    @Mock
    private LikeService likeService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletRequest httpRequest;

    @InjectMocks
    private LikeController likeController;

    // ==================== 点赞/取消点赞接口测试 ====================

    @Test
    @DisplayName("测试点赞 - 成功")
    void testToggleLike_Success() {
        LikeResponse response = new LikeResponse();
        response.setPostId(1L);
        response.setIsLiked(true);
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(likeService.toggleLike(anyLong(), anyLong())).thenReturn(response);

        ResponseEntity<?> result = likeController.toggleLike(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试点赞 - 未登录")
    void testToggleLike_NotLoggedIn() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ResponseEntity<?> result = likeController.toggleLike(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试点赞 - 服务异常")
    void testToggleLike_ServiceException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(likeService.toggleLike(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("数据库异常"));

        ResponseEntity<?> result = likeController.toggleLike(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取点赞数接口测试 ====================

    @Test
    @DisplayName("测试获取点赞数 - 成功")
    void testGetLikeCount_Success() {
        when(likeService.getLikeCount(anyLong())).thenReturn(50L);

        ResponseEntity<?> result = likeController.getLikeCount(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取点赞数 - 服务异常")
    void testGetLikeCount_ServiceException() {
        when(likeService.getLikeCount(anyLong()))
                .thenThrow(new RuntimeException("Elasticsearch异常"));

        ResponseEntity<?> result = likeController.getLikeCount(1L);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 获取点赞状态接口测试 ====================

    @Test
    @DisplayName("测试获取点赞状态 - 已点赞")
    void testGetLikeStatus_Liked() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(likeService.isLiked(anyLong(), anyLong())).thenReturn(true);

        ResponseEntity<?> result = likeController.getLikeStatus(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取点赞状态 - 未登录")
    void testGetLikeStatus_NotLoggedIn() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(null);

        ResponseEntity<?> result = likeController.getLikeStatus(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试获取点赞状态 - 服务异常")
    void testGetLikeStatus_ServiceException() {
        when(jwtUtil.extractUserId(anyString())).thenReturn(100L);
        when(likeService.isLiked(anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Redis异常"));

        ResponseEntity<?> result = likeController.getLikeStatus(1L, httpRequest);

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    // ==================== 同步点赞数接口测试 ====================

    @Test
    @DisplayName("测试同步点赞数 - 成功")
    void testSyncLikeCounts_Success() {
        doNothing().when(likeService).syncLikeCountsToDatabase(any());

        ResponseEntity<?> result = likeController.syncLikeCounts(Arrays.asList(1L, 2L, 3L));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }

    @Test
    @DisplayName("测试同步点赞数 - 服务异常")
    void testSyncLikeCounts_ServiceException() {
        doThrow(new RuntimeException("同步失败")).when(likeService).syncLikeCountsToDatabase(any());

        ResponseEntity<?> result = likeController.syncLikeCounts(Arrays.asList(1L));

        assertThat(result.getStatusCodeValue()).isEqualTo(200);
    }
}
