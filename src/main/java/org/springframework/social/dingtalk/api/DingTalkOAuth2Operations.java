package org.springframework.social.dingtalk.api;

import com.dingtalk.api.response.OapiSnsGetPersistentCodeResponse;
import org.springframework.social.oauth2.OAuth2Operations;

public interface DingTalkOAuth2Operations extends OAuth2Operations {
    String requestForOrRenewAccessToken();

    String concatenateAccessToken(OapiSnsGetPersistentCodeResponse persistentCodeResponse);

    OapiSnsGetPersistentCodeResponse splitAccessToken(String accessToken) ;
}
