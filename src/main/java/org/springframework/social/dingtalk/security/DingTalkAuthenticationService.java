package org.springframework.social.dingtalk.security;

import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.connect.DingTalkConnectionFactory;
import org.springframework.social.security.provider.OAuth2AuthenticationService;

public class DingTalkAuthenticationService extends OAuth2AuthenticationService<DingTalk> {
    public DingTalkAuthenticationService(String appId, String appSecret) {
        super(new DingTalkConnectionFactory(appId, appSecret));
        setDefaultScope(getConnectionFactory().getScope());
    }
}
