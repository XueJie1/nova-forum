package com.novaforum.nova_forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.novaforum.nova_forum.dto.CommentRequest;
import com.novaforum.nova_forum.dto.CommentResponse;
import com.novaforum.nova_forum.entity.Comment;

import java.util.List;

/**
 * 评论服务接口
 * 支持多层级评论结构和权限控制
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
public interface CommentService {

    /**
     * 创建评论
     * 
     * @param request 评论请求数据
     * @param userId  当前用户ID
     * @return 创建的评论ID
     */
    Long createComment(CommentRequest request, Long userId);

    /**
     * 更新评论
     * 
     * @param commentId 评论ID
     * @param content   新评论内容
     * @param userId    当前用户ID（用于权限验证）
     * @return 是否更新成功
     */
    boolean updateComment(Long commentId, String content, Long userId);

    /**
     * 删除评论
     * 
     * @param commentId 评论ID
     * @param userId    当前用户ID（用于权限验证）
     * @return 是否删除成功
     */
    boolean deleteComment(Long commentId, Long userId);

    /**
     * 获取评论详情
     * 
     * @param id 评论ID
     * @return 评论详情
     */
    Comment getCommentDetail(Long id);

    /**
     * 获取帖子的评论列表（树形结构）
     * 
     * @param postId    帖子ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 评论分页结果（树形结构）
     */
    IPage<CommentResponse> getCommentsByPostId(Long postId, Integer pageNum, Integer pageSize);

    /**
     * 获取用户的所有评论
     * 
     * @param userId    用户ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 评论分页结果
     */
    IPage<Comment> getCommentsByUserId(Long userId, Integer pageNum, Integer pageSize);

    /**
     * 获取评论的回复列表（子评论）
     * 
     * @param parentId  父评论ID
     * @param pageNum   页码
     * @param pageSize  每页大小
     * @return 子评论分页结果
     */
    IPage<CommentResponse> getRepliesByCommentId(Long parentId, Integer pageNum, Integer pageSize);

    /**
     * 获取帖子的评论总数
     * 
     * @param postId 帖子ID
     * @return 评论总数
     */
    Integer getCommentCountByPostId(Long postId);

    /**
     * 获取用户的评论总数
     * 
     * @param userId 用户ID
     * @return 评论总数
     */
    Integer getCommentCountByUserId(Long userId);

    /**
     * 获取评论的回复数量
     * 
     * @param commentId 评论ID
     * @return 回复数量
     */
    Integer getReplyCountByCommentId(Long commentId);

    /**
     * 验证评论权限
     * 
     * @param commentId 评论ID
     * @param userId    当前用户ID
     * @return 是否有权限操作
     */
    boolean canEditComment(Long commentId, Long userId);

    /**
     * 构建评论树形结构
     * 
     * @param comments 评论列表
     * @return 树形结构的评论列表
     */
    List<CommentResponse> buildCommentTree(List<Comment> comments);
}
