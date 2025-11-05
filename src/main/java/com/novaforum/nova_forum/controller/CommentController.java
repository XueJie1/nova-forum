package com.novaforum.nova_forum.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.novaforum.nova_forum.dto.ApiResponse;
import com.novaforum.nova_forum.dto.CommentRequest;
import com.novaforum.nova_forum.dto.CommentResponse;
import com.novaforum.nova_forum.entity.Comment;
import com.novaforum.nova_forum.service.CommentService;
import com.novaforum.nova_forum.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 评论控制器
 * 提供评论的CRUD操作和多层级回复功能
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Slf4j
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final JwtUtil jwtUtil;

    public CommentController(CommentService commentService, JwtUtil jwtUtil) {
        this.commentService = commentService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * 创建评论
     */
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Long>> createComment(
            @Valid @RequestBody CommentRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // 从请求头获取JWT Token并解析用户ID
            String token = extractTokenFromHeader(httpRequest);
            Long userId = jwtUtil.extractUserId(token);
            
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录或Token无效"));
            }

            Long commentId = commentService.createComment(request, userId);
            
            log.info("创建评论成功，评论ID: {}, 用户ID: {}", commentId, userId);
            return ResponseEntity.ok(ApiResponse.success(commentId));
            
        } catch (Exception e) {
            log.error("创建评论失败", e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 更新评论
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateComment(
            @PathVariable Long id,
            @RequestParam String content,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromHeader(httpRequest);
            Long userId = jwtUtil.extractUserId(token);
            
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录或Token无效"));
            }

            boolean success = commentService.updateComment(id, content, userId);
            
            if (success) {
                log.info("更新评论成功，评论ID: {}, 用户ID: {}", id, userId);
                return ResponseEntity.ok(ApiResponse.success("评论更新成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("评论更新失败"));
            }
            
        } catch (Exception e) {
            log.error("更新评论失败，评论ID: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteComment(
            @PathVariable Long id,
            HttpServletRequest httpRequest) {
        
        try {
            String token = extractTokenFromHeader(httpRequest);
            Long userId = jwtUtil.extractUserId(token);
            
            if (userId == null) {
                return ResponseEntity.ok(ApiResponse.error("用户未登录或Token无效"));
            }

            boolean success = commentService.deleteComment(id, userId);
            
            if (success) {
                log.info("删除评论成功，评论ID: {}, 用户ID: {}", id, userId);
                return ResponseEntity.ok(ApiResponse.success("评论删除成功"));
            } else {
                return ResponseEntity.ok(ApiResponse.error("评论删除失败"));
            }
            
        } catch (Exception e) {
            log.error("删除评论失败，评论ID: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取评论详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Comment>> getCommentDetail(@PathVariable Long id) {
        
        try {
            Comment comment = commentService.getCommentDetail(id);
            
            if (comment != null) {
                return ResponseEntity.ok(ApiResponse.success(comment));
            } else {
                return ResponseEntity.ok(ApiResponse.error("评论不存在"));
            }
            
        } catch (Exception e) {
            log.error("获取评论详情失败，评论ID: {}", id, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取帖子的评论列表
     */
    @GetMapping("/list/{postId}")
    public ResponseEntity<ApiResponse<IPage<CommentResponse>>> getCommentsByPostId(
            @PathVariable Long postId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            IPage<CommentResponse> comments = commentService.getCommentsByPostId(postId, pageNum, pageSize);
            
            return ResponseEntity.ok(ApiResponse.success(comments));
            
        } catch (Exception e) {
            log.error("获取评论列表失败，帖子ID: {}", postId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取用户的评论列表
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<IPage<Comment>>> getCommentsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            IPage<Comment> comments = commentService.getCommentsByUserId(userId, pageNum, pageSize);
            
            return ResponseEntity.ok(ApiResponse.success(comments));
            
        } catch (Exception e) {
            log.error("获取用户评论列表失败，用户ID: {}", userId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取评论的回复列表
     */
    @GetMapping("/replies/{parentId}")
    public ResponseEntity<ApiResponse<IPage<CommentResponse>>> getRepliesByCommentId(
            @PathVariable Long parentId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        try {
            IPage<CommentResponse> replies = commentService.getRepliesByCommentId(parentId, pageNum, pageSize);
            
            return ResponseEntity.ok(ApiResponse.success(replies));
            
        } catch (Exception e) {
            log.error("获取回复列表失败，父评论ID: {}", parentId, e);
            return ResponseEntity.ok(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * 获取帖子评论总数
     */
    @GetMapping("/count/{postId}")
    public ResponseEntity<ApiResponse<Integer>> getCommentCount(@PathVariable Long postId) {
        
        try {
            Integer count = commentService.getCommentCountByPostId(postId);
            
            return ResponseEntity.ok(ApiResponse.success(count));
            
        } catch (Exception e) {
            log.error("获取评论数量失败，帖子ID: {}", postId, e);
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
