package org.springframework.social.dingtalk.util;

import com.dingtalk.api.response.OapiSnsGetPersistentCodeResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;

public class AccessTokenUtil {
    public static String concatenateAccessToken(OapiSnsGetPersistentCodeResponse persistentCodeResponse) {
        return persistentCodeResponse.getOpenid() + ":" + persistentCodeResponse.getUnionid() + ":" + persistentCodeResponse.getPersistentCode();
    }

    public static OapiSnsGetPersistentCodeResponse splitAccessToken(String accessToken) {
        final OapiSnsGetPersistentCodeResponse persistentCodeResponse = new OapiSnsGetPersistentCodeResponse();
        final String[] split = accessToken.split(":");
        persistentCodeResponse.setOpenid(split[0]);
        persistentCodeResponse.setUnionid(split[1]);
        persistentCodeResponse.setPersistentCode(split[2]);
        return persistentCodeResponse;
    }

    public static String concatenateAccessToken(OapiSnsGetuserinfoBycodeResponse.UserInfo userInfo) {
        return userInfo.getOpenid() + ":" + userInfo.getUnionid() + ":" + userInfo.getNick();
    }

    public static OapiSnsGetuserinfoBycodeResponse.UserInfo extractUserInfoFromAccessToken(String accessToken) {
        final OapiSnsGetuserinfoBycodeResponse.UserInfo userInfo = new OapiSnsGetuserinfoBycodeResponse.UserInfo();
        final String[] split = accessToken.split(":");
        userInfo.setOpenid(split[0]);
        userInfo.setUnionid(split[1]);
        userInfo.setNick(split[2]);
        return userInfo;
    }
}
