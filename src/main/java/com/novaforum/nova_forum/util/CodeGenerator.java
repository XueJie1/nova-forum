package com.novaforum.nova_forum.util;

import java.util.Random;

/**
 * 验证码生成工具类
 */
public class CodeGenerator {

    private static final Random RANDOM = new Random();

    /**
     * 生成6位数字验证码
     */
    public static String generate6DigitCode() {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            code.append(RANDOM.nextInt(10));
        }
        return code.toString();
    }

    /**
     * 验证验证码格式
     */
    public static boolean isValidCode(String code) {
        if (code == null || code.length() != 6) {
            return false;
        }

        try {
            Integer.parseInt(code);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
