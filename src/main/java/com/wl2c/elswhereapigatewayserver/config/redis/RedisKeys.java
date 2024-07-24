package com.wl2c.elswhereapigatewayserver.config.redis;

public class RedisKeys {
    public static final String KEY_DELIMITER = ":";
    public static final String SIGNUP_AUTH_KEY = "signupAuth";

    public static String combine(Object key1, Object key2) {
        return key1 + KEY_DELIMITER + key2;
    }

    public static String combine(Object key1, Object key2, Object key3) {
        return key1 + KEY_DELIMITER + key2 + KEY_DELIMITER + key3;
    }

    public static String combine(Object... keys) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < keys.length; i++) {
            sb.append(keys[i]);
            if (i < keys.length - 1) {
                sb.append(KEY_DELIMITER);
            }
        }
        return sb.toString();
    }
}
