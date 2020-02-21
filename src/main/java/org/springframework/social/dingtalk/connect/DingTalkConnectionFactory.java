package org.springframework.social.dingtalk.connect;

import org.springframework.social.connect.support.OAuth2ConnectionFactory;
import org.springframework.social.dingtalk.api.DingTalk;

public class DingTalkConnectionFactory extends OAuth2ConnectionFactory<DingTalk> {
    public DingTalkConnectionFactory(String appId, String appSecret) {
        super("dingtalk", new DingTalkServiceProvider(appId, appSecret), new DingTalkAdapter());
        setScope("snsapi_login");
    }
}
