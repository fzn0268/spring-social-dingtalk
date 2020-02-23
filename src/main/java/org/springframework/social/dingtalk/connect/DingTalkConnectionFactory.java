package org.springframework.social.dingtalk.connect;

import org.springframework.social.connect.Connection;
import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.connect.support.DingTalkConnection;
import org.springframework.social.dingtalk.util.AccessTokenUtil;
import org.springframework.social.oauth2.AccessGrant;

public class DingTalkConnectionFactory extends OAuth2ConnectionFactory<DingTalk> {
    private boolean persistent;

    public DingTalkConnectionFactory(String appId, String appSecret, boolean qrCodeLogin, boolean persistent) {
        super("dingtalk", new DingTalkServiceProvider(appId, appSecret, qrCodeLogin, persistent), persistent ? new DingTalkAdapter() : null);
        setScope("snsapi_login");
        this.persistent = persistent;
    }

    @Override
    public Connection<DingTalk> createConnection(AccessGrant accessGrant) {
        if (persistent) {
            return super.createConnection(accessGrant);
        }
        return new DingTalkConnection(getProviderId(), extractProviderUserId(accessGrant), accessGrant.getAccessToken(),
                accessGrant.getRefreshToken(), accessGrant.getExpireTime(), (DingTalkServiceProvider) getServiceProvider(), getApiAdapter());
    }

    @Override
    protected String extractProviderUserId(AccessGrant accessGrant) {
        final String accessToken = accessGrant.getAccessToken();
        return AccessTokenUtil.splitAccessToken(accessToken).getOpenid();
    }
}
