package org.springframework.social.dingtalk.util;

public class DingTalkApiUriUtil {
    public static final String BASE_URL = "https://oapi.dingtalk.com";

    public static String buildUri(String uri) {
        return BASE_URL + uri;
    }
}
