package com.novaforum.nova_forum.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.novaforum.nova_forum.entity.PostLike;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子点赞记录Mapper接口
 */
public interface PostLikeMapper extends BaseMapper<PostLike> {

    /**
     * 批量插入点赞记录
     */
    int insertBatch(@Param("list") List<PostLike> list);

    /**
     * 根据帖子ID删除所有点赞记录
     */
    int deleteByPostId(@Param("postId") Long postId);

    /**
     * 根据用户ID删除所有点赞记录
     */
    int deleteByUserId(@Param("userId") Long userId);
}
