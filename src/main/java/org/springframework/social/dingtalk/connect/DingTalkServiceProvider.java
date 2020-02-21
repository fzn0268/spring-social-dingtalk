package org.springframework.social.dingtalk.connect;

import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.api.DingTalkOAuth2Operations;
import org.springframework.social.dingtalk.api.impl.DingTalkOAuth2Template;
import org.springframework.social.dingtalk.api.impl.DingTalkTemplate;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.social.oauth2.AbstractOAuth2ServiceProvider;

public class DingTalkServiceProvider extends AbstractOAuth2ServiceProvider<DingTalk> {
    private String appId;

    private String appSecret;

    public DingTalkServiceProvider(String appId, String appSecret) {
        super(new DingTalkOAuth2Template(appId, appSecret, DingTalkApiUriUtil.buildUri("/connect/oauth2/sns_authorize"), DingTalkApiUriUtil.buildUri("/sns/gettoken"), DingTalkApiUriUtil.buildUri("/sns/get_persistent_code")));
        this.appId = appId;
        this.appSecret = appSecret;
    }

    @Override
    public DingTalk getApi(String accessToken) {
        return new DingTalkTemplate(accessToken, appId, appSecret, (DingTalkOAuth2Operations) getOAuthOperations());
    }
}
