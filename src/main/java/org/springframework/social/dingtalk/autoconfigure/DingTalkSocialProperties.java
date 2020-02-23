package org.springframework.social.dingtalk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.social.dingtalk")
public class DingTalkSocialProperties {
    private String appId;

    private String appSecret;

    private boolean qrCodeLogin = true;

    private boolean persistent = false;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getAppSecret() {
        return appSecret;
    }

    public void setAppSecret(String appSecret) {
        this.appSecret = appSecret;
    }

    public boolean isQrCodeLogin() {
        return qrCodeLogin;
    }

    public void setQrCodeLogin(boolean qrCodeLogin) {
        this.qrCodeLogin = qrCodeLogin;
    }

    public boolean isPersistent() {
        return persistent;
    }

    public void setPersistent(boolean persistent) {
        this.persistent = persistent;
    }
}
