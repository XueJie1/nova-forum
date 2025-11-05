package com.novaforum.nova_forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.entity.Comment;

/**
 * 评论数据访问层接口
 * 支持多层级评论查询和管理
 *
 * @author Nova Forum Team
 * @since 2025-11-05
 */
@Mapper
public interface CommentMapper extends BaseMapper<Comment> {

    /**
     * 根据帖子ID分页查询评论列表
     * 
     * @param page    分页参数
     * @param postId  帖子ID
     * @param parentId 父评论ID（null表示顶级评论）
     * @return 评论分页结果
     */
    IPage<Comment> selectCommentsByPostIdPage(Page<Comment> page, 
                                            @Param("postId") Long postId, 
                                            @Param("parentId") Long parentId);

    /**
     * 查询帖子的所有评论（构建树形结构用）
     * 
     * @param postId 帖子ID
     * @return 评论列表
     */
    java.util.List<Comment> selectCommentsByPostId(@Param("postId") Long postId);

    /**
     * 根据ID查询评论详情（包含用户信息）
     * 
     * @param id 评论ID
     * @return 评论详情
     */
    Comment selectCommentWithUserById(@Param("id") Long id);

    /**
     * 查询评论的子评论数量
     * 
     * @param parentId 父评论ID
     * @return 子评论数量
     */
    int selectReplyCountByParentId(@Param("parentId") Long parentId);

    /**
     * 查询帖子的评论总数
     * 
     * @param postId 帖子ID
     * @return 评论总数
     */
    int selectCommentCountByPostId(@Param("postId") Long postId);

    /**
     * 查询用户的所有评论数量
     * 
     * @param userId 用户ID
     * @return 评论总数
     */
    int selectCommentCountByUserId(@Param("userId") Long userId);

    /**
     * 批量删除评论及其子评论（逻辑删除）
     * 
     * @param ids 评论ID列表
     * @return 影响行数
     */
    int deleteCommentsBatch(@Param("ids") java.util.List<Long> ids);

    /**
     * 根据帖子ID删除所有评论
     * 
     * @param postId 帖子ID
     * @return 影响行数
     */
    int deleteCommentsByPostId(@Param("postId") Long postId);

    /**
     * 根据用户ID删除所有评论
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteCommentsByUserId(@Param("userId") Long userId);
}
