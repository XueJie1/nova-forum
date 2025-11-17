package com.novaforum.nova_forum.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 邮箱验证响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailVerifyResponse {

    private Boolean isValid;
    private String message;

    public static EmailVerifyResponse success() {
        return new EmailVerifyResponse(true, "邮箱验证成功");
    }

    public static EmailVerifyResponse failed(String message) {
        return new EmailVerifyResponse(false, message);
    }
}
