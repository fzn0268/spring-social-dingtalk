package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.request.OapiSnsGetSnsTokenRequest;
import com.dingtalk.api.response.OapiSnsGetPersistentCodeResponse;
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
import org.springframework.social.support.URIBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.util.concurrent.TimeUnit;

public class DingTalkTemplate extends AbstractOAuth2ApiBinding implements DingTalk {
    private static final long TIMEOUT_SNS_TOKEN = TimeUnit.HOURS.toMillis(2);

    private String appId;

    private String appSecret;

    private String openId;

    private String persistentCode;

    private volatile String snsToken;

    private volatile long lastSnsTokenReqTime;

    private DingTalkOAuth2Operations oAuth2Operations;

    private UserOperations userOperations;

    public DingTalkTemplate(String accessToken, String appId, String appSecret, DingTalkOAuth2Operations oAuthOperations) {
        super(accessToken, TokenStrategy.ACCESS_TOKEN_PARAMETER);
        this.appId = appId;
        this.appSecret = appSecret;
        final OapiSnsGetPersistentCodeResponse persistentCodeResponse = oAuthOperations.splitAccessToken(accessToken);
        this.openId = persistentCodeResponse.getOpenid();
        this.persistentCode = persistentCodeResponse.getPersistentCode();
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
        final String accessToken = oAuth2Operations.requestForOrRenewAccessToken();
        final OapiSnsGetSnsTokenRequest getSnsTokenRequest = new OapiSnsGetSnsTokenRequest();
        getSnsTokenRequest.setOpenid(openId);
        getSnsTokenRequest.setPersistentCode(persistentCode);
        final MultiValueMap<String, String> queryParams = new LinkedMultiValueMap<>();
        queryParams.add("access_token", accessToken);
        final OapiSnsGetSnsTokenResponse snsTokenResponse = getRestTemplate().postForObject(URIBuilder.fromUri(DingTalkApiUriUtil.buildUri("/sns/get_sns_token")).queryParams(queryParams).build().toString(), getSnsTokenRequest, OapiSnsGetSnsTokenResponse.class);
        snsToken = snsTokenResponse.getSnsToken();
        lastSnsTokenReqTime = System.currentTimeMillis();
        return snsToken;
    }
}
