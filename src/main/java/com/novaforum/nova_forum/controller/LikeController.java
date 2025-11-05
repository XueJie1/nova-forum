package com.novaforum.nova_forum.controller;

import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.LikeResponse;
import com.novaforum.nova_forum.service.LikeService;
import com.novaforum.nova_forum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 点赞控制器
 * 提供点赞/取消点赞和查询功能
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Slf4j
@RestController
@RequestMapping("/like")
public class LikeController {

    private final LikeService likeService;
    private final JwtUtil jwtUtil;

    public LikeController(LikeService likeService, JwtUtil jwtUtil) {
        this.likeService = likeService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 点赞/取消点赞帖子
     */
    @PostMapping("/{postId}")
    public ResponseEntity<ApiResponse<LikeResponse>> toggleLike(
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        try {
            // 从请求头获取JWT Token并解析用户ID
            String token = extractTokenFromHeader(request);
            Long userId = jwtUtil.extractUserId(token);
            
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录或Token无效"));
            }

            LikeResponse response = likeService.toggleLike(postId, userId);
            
            String action = response.getIsLiked() ? "点赞" : "取消点赞";
            log.info("用户 {} {} 帖子 {} 成功", userId, action, postId);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            log.error("点赞操作失败，帖子ID: {}", postId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取帖子点赞数
     */
    @GetMapping("/count/{postId}")
    public ResponseEntity<ApiResponse<Long>> getLikeCount(@PathVariable Long postId) {
        
        try {
            Long count = likeService.getLikeCount(postId);
            
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("获取点赞数失败，帖子ID: {}", postId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户点赞状态
     */
    @GetMapping("/status/{postId}")
    public ResponseEntity<ApiResponse<Boolean>> getLikeStatus(
            @PathVariable Long postId,
            HttpServletRequest request) {
        
        try {
            // 从请求头获取JWT Token并解析用户ID
            String token = extractTokenFromHeader(request);
            Long userId = jwtUtil.extractUserId(token);
            
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录或Token无效"));
            }

            boolean isLiked = likeService.isLiked(postId, userId);
            
            return ResponseEntity.ok(ApiResponse.success(isLiked));
            
        } catch (Exception e) {
            log.error("获取点赞状态失败，帖子ID: {}", postId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 同步点赞数到数据库（管理员功能）
     */
    @PostMapping("/sync")
    public ResponseEntity<ApiResponse<String>> syncLikeCounts(
            @RequestParam java.util.List<Long> postIds) {
        
        try {
            likeService.syncLikeCountsToDatabase(postIds);
            
            log.info("同步 {} 个帖子的点赞数到数据库", postIds.size());
            return ResponseEntity.ok(ApiResponse.success("点赞数同步成功"));
            
        } catch (Exception e) {
            log.error("同步点赞数失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 从请求头中提取JWT Token
     */
    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
