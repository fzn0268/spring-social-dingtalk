package org.springframework.social.dingtalk.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.social.autoconfigure.SocialProperties;

@ConfigurationProperties(prefix = "spring.social.dingtalk")
public class DingTalkSocialProperties extends SocialProperties {
}
