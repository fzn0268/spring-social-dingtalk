package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.request.OapiSnsGetuserinfoRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoResponse;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.api.UserOperations;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.web.client.RestTemplate;

public class UserTemplate implements UserOperations {
    private final DingTalk dingTalk;

    private final RestTemplate restTemplate;

    public UserTemplate(DingTalk dingTalk, RestTemplate restTemplate) {
        this.dingTalk = dingTalk;
        this.restTemplate = restTemplate;
    }

    @Override
    public OapiSnsGetuserinfoBycodeResponse getUserInfo(OapiSnsGetuserinfoBycodeRequest userGetRequest) {
        return restTemplate.getForObject(DingTalkApiUriUtil.buildUri("/sns/getuserinfo_bycode"), OapiSnsGetuserinfoBycodeResponse.class, userGetRequest.getTextParams());
    }

    @Override
    public OapiSnsGetuserinfoResponse getUserInfo() {
        final String snsToken = dingTalk.requestForOrRenewSnsToken();
        final OapiSnsGetuserinfoRequest getUserInfoRequest = new OapiSnsGetuserinfoRequest();
        getUserInfoRequest.setSnsToken(snsToken);
        return restTemplate.getForObject(DingTalkApiUriUtil.buildUri("/sns/getuserinfo"), OapiSnsGetuserinfoResponse.class, getUserInfoRequest.getTextParams());
    }
}
