package org.springframework.social.dingtalk.api;

import org.springframework.social.oauth2.OAuth2Operations;

public interface DingTalkOAuth2Operations extends OAuth2Operations {
    String requestForOrRenewAccessToken();
}
