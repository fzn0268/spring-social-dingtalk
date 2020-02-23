package org.springframework.social.dingtalk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.social.autoconfigure.SocialProperties;

@ConfigurationProperties(prefix = "spring.social.dingtalk")
public class DingTalkSocialProperties extends SocialProperties {
    private boolean qrCodeLogin = true;

    private boolean persistent = false;

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
