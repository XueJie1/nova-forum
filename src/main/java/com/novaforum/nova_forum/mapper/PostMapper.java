package com.novaforum.nova_forum.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.novaforum.nova_forum.entity.Post;

@Mapper
public interface PostMapper extends BaseMapper<Post> {

    /**
     * 分页查询帖子列表
     * 
     * @param page   分页参数
     * @param userId 用户ID（可选，用于查询特定用户的帖子）
     * @return 帖子分页结果
     */
    IPage<Post> selectPostsPage(Page<Post> page, @Param("userId") Long userId);

    /**
     * 根据ID查询帖子详情（包含作者信息）
     * 
     * @param id 帖子ID
     * @return 帖子详情
     */
    Post selectPostWithAuthorById(@Param("id") Long id);

    /**
     * 增加浏览次数
     * 
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementViewCount(@Param("id") Long id);

    /**
     * 增加点赞次数
     * 
     * @param id 帖子ID
     * @return 影响行数
     */
    int incrementLikeCount(@Param("id") Long id);

    /**
     * 减少点赞次数
     * 
     * @param id 帖子ID
     * @return 影响行数
     */
    int decrementLikeCount(@Param("id") Long id);
}
