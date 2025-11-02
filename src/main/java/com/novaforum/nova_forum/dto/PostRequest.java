package com.novaforum.nova_forum.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 帖子请求DTO
 */
public class PostRequest {

    @NotBlank(message = "标题不能为空")
    @Size(min = 1, max = 200, message = "标题长度必须在1-200个字符之间")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(min = 1, max = 5000, message = "内容长度必须在1-5000个字符之间")
    private String content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
