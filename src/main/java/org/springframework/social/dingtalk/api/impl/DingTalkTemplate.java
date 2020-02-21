package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.request.OapiSnsGetSnsTokenRequest;
import com.dingtalk.api.response.OapiSnsGetSnsTokenResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.api.DingTalkOAuth2Operations;
import org.springframework.social.dingtalk.api.UserOperations;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.social.oauth2.AbstractOAuth2ApiBinding;
import org.springframework.social.oauth2.TokenStrategy;
import org.springframework.social.support.ClientHttpRequestFactorySelector;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DingTalkTemplate extends AbstractOAuth2ApiBinding implements DingTalk {
    private static final long TIMEOUT_SNS_TOKEN = TimeUnit.HOURS.toMillis(2);

    private String appId;

    private String appSecret;

    private volatile String accessToken;

    private String openId;

    private String persistentCode;

    private volatile String snsToken;

    private volatile long lastSnsTokenReqTime;

    private DingTalkOAuth2Operations oAuth2Operations;

    private UserOperations userOperations;

    public DingTalkTemplate(String persistentCode, String appId, String appSecret, DingTalkOAuth2Operations oAuthOperations) {
        super(persistentCode, TokenStrategy.ACCESS_TOKEN_PARAMETER);
        this.appId = appId;
        this.appSecret = appSecret;
        final String[] split = persistentCode.split(":");
        this.openId = split[0];
        this.persistentCode = split[2];
        this.oAuth2Operations = oAuthOperations;
        initialize();
    }

    @Override
    protected MappingJackson2HttpMessageConverter getJsonMessageConverter() {
        return new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE).build());
    }

    @Override
    public UserOperations userOperations() {
        return userOperations;
    }

    private void initialize() {
        super.setRequestFactory(ClientHttpRequestFactorySelector.bufferRequests(getRestTemplate().getRequestFactory()));
        userOperations = new UserTemplate(this, getRestTemplate());
    }

    @Override
    public String requestForOrRenewSnsToken() {
        if (lastSnsTokenReqTime != 0 && System.currentTimeMillis() - lastSnsTokenReqTime < TIMEOUT_SNS_TOKEN) {
            return snsToken;
        }
        oAuth2Operations.requestForOrRenewAccessToken();
        final OapiSnsGetSnsTokenRequest getSnsTokenRequest = new OapiSnsGetSnsTokenRequest();
        getSnsTokenRequest.setOpenid(openId);
        getSnsTokenRequest.setPersistentCode(persistentCode);
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("access_token", accessToken);
        final OapiSnsGetSnsTokenResponse snsTokenResponse = getRestTemplate().postForObject(DingTalkApiUriUtil.buildUri("/sns/get_sns_token"), getSnsTokenRequest, OapiSnsGetSnsTokenResponse.class, queryParams);
        snsToken = snsTokenResponse.getSnsToken();
        lastSnsTokenReqTime = System.currentTimeMillis();
        return snsToken;
    }
}
