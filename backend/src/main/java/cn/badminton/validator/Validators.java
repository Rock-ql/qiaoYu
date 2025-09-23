package cn.badminton.validator;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * 简单校验工具类
 *
 * 作者: xiaolei
 */
public final class Validators {
    private Validators() {}

    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9][0-9]{9}$");

    public static void notBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void isPhone(String value, String message) {
        if (value == null || !PHONE_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void minLength(String value, int len, String message) {
        if (value == null || value.trim().length() < len) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void notEmpty(Collection<?> c, String message) {
        if (c == null || c.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }
}
