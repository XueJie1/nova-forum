package com.novaforum.nova_forum.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.novaforum.nova_forum.entity.Post;

/**
 * 帖子服务接口
 */
public interface PostService {

    /**
     * 创建帖子
     * 
     * @param post   帖子信息
     * @param userId 作者ID
     * @return 创建的帖子ID
     */
    Long createPost(Post post, Long userId);

    /**
     * 更新帖子
     * 
     * @param post   帖子信息
     * @param userId 当前用户ID（用于权限验证）
     * @return 是否更新成功
     */
    boolean updatePost(Post post, Long userId);

    /**
     * 删除帖子
     * 
     * @param postId 帖子ID
     * @param userId 当前用户ID（用于权限验证）
     * @return 是否删除成功
     */
    boolean deletePost(Long postId, Long userId);

    /**
     * 获取帖子详情
     * 
     * @param id 帖子ID
     * @return 帖子详情
     */
    Post getPostDetail(Long id);

    /**
     * 分页获取帖子列表
     * 
     * @param pageNum  页码
     * @param pageSize 每页大小
     * @param userId   用户ID（可选，用于查询特定用户的帖子）
     * @return 帖子分页结果
     */
    IPage<Post> getPostList(Integer pageNum, Integer pageSize, Long userId);

    /**
     * 增加浏览次数
     * 
     * @param id 帖子ID
     */
    void incrementViewCount(Long id);

    /**
     * 增加点赞次数
     * 
     * @param id 帖子ID
     */
    void incrementLikeCount(Long id);

    /**
     * 减少点赞次数
     * 
     * @param id 帖子ID
     */
    void decrementLikeCount(Long id);
}
