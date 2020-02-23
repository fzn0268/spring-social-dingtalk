package org.springframework.social.dingtalk.autoconfigure;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.core.env.Environment;
import org.springframework.social.UserIdSource;
import org.springframework.social.autoconfigure.SocialWebAutoConfiguration;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurer;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.*;
import org.springframework.social.connect.mem.InMemoryUsersConnectionRepository;
import org.springframework.social.connect.web.GenericConnectionStatusView;
import org.springframework.social.dingtalk.api.DingTalk;
import org.springframework.social.dingtalk.connect.DingTalkConnectionFactory;

@Configuration
@ConditionalOnClass({ SocialConfigurerAdapter.class, DingTalkConnectionFactory.class })
@ConditionalOnProperty(prefix = "spring.social.dingtalk", name = "app-id")
@EnableConfigurationProperties(DingTalkSocialProperties.class)
@AutoConfigureBefore(SocialWebAutoConfiguration.class)
@AutoConfigureAfter(WebMvcAutoConfiguration.class)
public class DingTalkSocialAutoConfiguration {

    @Configuration
    @EnableSocial
    @EnableConfigurationProperties(DingTalkSocialProperties.class)
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    protected static class DingTalkConfigurerAdapter implements SocialConfigurer {
        private final DingTalkSocialProperties properties;

        public DingTalkConfigurerAdapter(DingTalkSocialProperties properties) {
            this.properties = properties;
        }

        @Bean
        @ConditionalOnMissingBean(DingTalk.class)
        @Scope(value = "request", proxyMode = ScopedProxyMode.INTERFACES)
        public DingTalk facebook(ConnectionRepository repository) {
            Connection<DingTalk> connection = repository
                    .findPrimaryConnection(DingTalk.class);
            return connection != null ? connection.getApi() : null;
        }

        @Bean(name = { "connect/dingtalkConnect", "connect/dingtalkConnected" })
        @ConditionalOnProperty(prefix = "spring.social", name = "auto-connection-views")
        public GenericConnectionStatusView facebookConnectView() {
            return new GenericConnectionStatusView("dingtalk", "DingTalk");
        }

        protected ConnectionFactory<?> createConnectionFactory() {
            return new DingTalkConnectionFactory(properties.getAppId(), properties.getAppSecret(), properties.isQrCodeLogin(), properties.isPersistent());
        }

        @Override
        public void addConnectionFactories(ConnectionFactoryConfigurer connectionFactoryConfigurer, Environment environment) {
            connectionFactoryConfigurer.addConnectionFactory(createConnectionFactory());
        }

        @Override
        public UserIdSource getUserIdSource() {
            return null;
        }

        @Override
        public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
            return new InMemoryUsersConnectionRepository(connectionFactoryLocator);
        }
    }
}
