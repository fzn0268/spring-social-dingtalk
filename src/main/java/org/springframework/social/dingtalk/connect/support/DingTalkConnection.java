package org.springframework.social.dingtalk.connect.support;

import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.support.OAuth2Connection;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.util.AccessTokenUtil;
import org.springframework.social.oauth2.OAuth2ServiceProvider;

public class DingTalkConnection extends OAuth2Connection<DingTalk> {
    private String openId;

    private String unionId;

    private String nick;

    public DingTalkConnection(String providerId, String providerUserId, String accessToken, String refreshToken, Long expireTime, OAuth2ServiceProvider<DingTalk> serviceProvider, ApiAdapter<DingTalk> apiAdapter) {
        super(providerId, providerUserId, accessToken, refreshToken, expireTime, serviceProvider, apiAdapter);
        final OapiSnsGetuserinfoBycodeResponse.UserInfo userInfo = AccessTokenUtil.extractUserInfoFromAccessToken(accessToken);
        openId = userInfo.getOpenid();
        unionId = userInfo.getUnionid();
        nick = userInfo.getNick();
    }

    @Override
    public void refresh() {
    }

    @Override
    public String getDisplayName() {
        return nick;
    }

    @Override
    public String getProfileUrl() {
        return "";
    }

    @Override
    public String getImageUrl() {
        return "";
    }

    @Override
    public void sync() {
    }
}
