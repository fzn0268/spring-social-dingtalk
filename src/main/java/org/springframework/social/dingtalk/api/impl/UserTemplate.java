package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiSnsGetuserinfoRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoResponse;
import org.springframework.social.ApiException;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.api.UserOperations;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.social.support.URIBuilder;

public class UserTemplate implements UserOperations {
    private final DingTalk dingTalk;

    public UserTemplate(DingTalk dingTalk) {
        this.dingTalk = dingTalk;
    }

    @Override
    public OapiSnsGetuserinfoResponse getUserInfo() {
        final String snsToken = dingTalk.requestForOrRenewSnsToken();
        final OapiSnsGetuserinfoRequest getUserInfoRequest = new OapiSnsGetuserinfoRequest();
        getUserInfoRequest.setSnsToken(snsToken);
        final DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(URIBuilder.fromUri(DingTalkApiUriUtil.buildUri("/sns/getuserinfo")).build().toString());
        try {
            return dingTalkClient.execute(getUserInfoRequest);
        } catch (com.taobao.api.ApiException e) {
            throw new ApiException("dingtalk", "Failed to fetch user info", e);
        }
    }
}
