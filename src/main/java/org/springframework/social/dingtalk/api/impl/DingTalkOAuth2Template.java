package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiSnsGetPersistentCodeRequest;
import com.dingtalk.api.request.OapiSnsGettokenRequest;
import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.response.OapiSnsGetPersistentCodeResponse;
import com.dingtalk.api.response.OapiSnsGettokenResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import org.springframework.social.ApiException;
import org.springframework.social.dingtalk.api.DingTalkOAuth2Operations;
import org.springframework.social.dingtalk.util.AccessTokenUtil;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.social.oauth2.AccessGrant;
import org.springframework.social.oauth2.GrantType;
import org.springframework.social.oauth2.OAuth2Parameters;
import org.springframework.social.oauth2.OAuth2Template;
import org.springframework.social.support.URIBuilder;
import org.springframework.util.Assert;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DingTalkOAuth2Template extends OAuth2Template implements DingTalkOAuth2Operations {
    private static final long TIMEOUT_ACCESS_TOKEN = TimeUnit.HOURS.toMillis(2);

    private final String appId;

    private final String appSecret;

    private final String accessTokenUrl;

    private volatile String accessToken;

    private volatile long lastAccessTokenReqTime;

    private final String authorizeUrl;

    private String authenticateUrl;

    private boolean persistent;

    private final String getPersistentCodeUrl;

    private final String getUserInfoByCodeUrl;

    /**
     * Constructs an OAuth2Template for a given set of client credentials.
     * Assumes that the authorization URL is the same as the authentication URL.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token.
     */
    public DingTalkOAuth2Template(String appId, String appSecret, String authorizeUrl, String accessTokenUrl, boolean persistent) {
        this(appId, appSecret, authorizeUrl, null, accessTokenUrl, DingTalkApiUriUtil.buildUri("/sns/get_persistent_code"), DingTalkApiUriUtil.buildUri("/sns/getuserinfo_bycode"), persistent);
    }

    /**
     * Constructs an OAuth2Template for a given set of client credentials.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param authenticateUrl the URL to redirect to when doing authentication via authorization code grant
     * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token
     */
    public DingTalkOAuth2Template(String appId, String appSecret, String authorizeUrl, String authenticateUrl, String accessTokenUrl, String getPersistentCodeUrl, String getUserInfoByCodeUrl, boolean persistent) {
        super(appId, appSecret, authorizeUrl, authenticateUrl, accessTokenUrl);
        Assert.notNull(appId, "The clientId property cannot be null");
        Assert.notNull(appSecret, "The clientSecret property cannot be null");
        Assert.notNull(authorizeUrl, "The authorizeUrl property cannot be null");
        Assert.notNull(accessTokenUrl, "The accessTokenUrl property cannot be null");
        this.appId = appId;
        this.appSecret = appSecret;
        String clientInfo = "?appid=" + formEncode(appId);
        this.authorizeUrl = authorizeUrl + clientInfo;
        if (authenticateUrl != null) {
            this.authenticateUrl = authenticateUrl + clientInfo;
        } else {
            this.authenticateUrl = null;
        }
        this.accessTokenUrl = accessTokenUrl;
        this.getPersistentCodeUrl = getPersistentCodeUrl;
        this.getUserInfoByCodeUrl = getUserInfoByCodeUrl;
        this.persistent = persistent;
    }

    public String buildAuthorizeUrl(OAuth2Parameters parameters) {
        return buildAuthUrl(authorizeUrl, GrantType.AUTHORIZATION_CODE, parameters);
    }

    public String buildAuthorizeUrl(GrantType grantType, OAuth2Parameters parameters) {
        return buildAuthUrl(authorizeUrl, grantType, parameters);
    }

    public String buildAuthenticateUrl(OAuth2Parameters parameters) {
        return authenticateUrl != null ? buildAuthUrl(authenticateUrl, GrantType.AUTHORIZATION_CODE, parameters) : buildAuthorizeUrl(GrantType.AUTHORIZATION_CODE, parameters);
    }

    public String buildAuthenticateUrl(GrantType grantType, OAuth2Parameters parameters) {
        return authenticateUrl != null ? buildAuthUrl(authenticateUrl, grantType, parameters) : buildAuthorizeUrl(grantType, parameters);
    }

    public AccessGrant exchangeForAccess(String authorizationCode, String redirectUri, MultiValueMap<String, String> additionalParameters) {
        if (persistent) {
            requestForOrRenewAccessToken();
            final OapiSnsGetPersistentCodeRequest persistentCodeRequest = new OapiSnsGetPersistentCodeRequest();
            persistentCodeRequest.setTmpAuthCode(authorizationCode);
            final DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(URIBuilder.fromUri(getPersistentCodeUrl).build().toString());
            try {
                final OapiSnsGetPersistentCodeResponse persistentCodeResponse = dingTalkClient.execute(persistentCodeRequest, accessToken);
                return new AccessGrant(AccessTokenUtil.concatenateAccessToken(persistentCodeResponse));
            } catch (com.taobao.api.ApiException e) {
                throw new ApiException("dingtalk", "Failed to request for persistent code", e);
            }
        }
        final OapiSnsGetuserinfoBycodeRequest getUserInfoByCodeRequest = new OapiSnsGetuserinfoBycodeRequest();
        getUserInfoByCodeRequest.setTmpAuthCode(authorizationCode);
        final DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(URIBuilder.fromUri(getUserInfoByCodeUrl).build().toString());
        try {
            final OapiSnsGetuserinfoBycodeResponse.UserInfo userInfo = dingTalkClient.execute(getUserInfoByCodeRequest, appId, appSecret).getUserInfo();
            return new AccessGrant(AccessTokenUtil.concatenateAccessToken(userInfo));
        } catch (com.taobao.api.ApiException e) {
            throw new ApiException("dingtalk", "Failed to fetch user info", e);
        }
    }

    public AccessGrant exchangeCredentialsForAccess(String username, String password, MultiValueMap<String, String> additionalParameters) {
        throw new UnsupportedOperationException();
    }

    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        throw new UnsupportedOperationException();
    }

    public AccessGrant authenticateClient(String scope) {
        throw new UnsupportedOperationException();
    }

    // internal helpers

    private String buildAuthUrl(String baseAuthUrl, GrantType grantType, OAuth2Parameters parameters) {
        StringBuilder authUrl = new StringBuilder(baseAuthUrl);
        if (grantType == GrantType.AUTHORIZATION_CODE) {
            authUrl.append('&').append("response_type").append('=').append("code");
        } else if (grantType == GrantType.IMPLICIT_GRANT) {
            throw new UnsupportedOperationException();
        }
        for (Iterator<Map.Entry<String, List<String>>> additionalParams = parameters.entrySet().iterator(); additionalParams.hasNext();) {
            Map.Entry<String, List<String>> param = additionalParams.next();
            String name = formEncode(param.getKey());
            for (Iterator<String> values = param.getValue().iterator(); values.hasNext();) {
                authUrl.append('&').append(name);
                String value = values.next();
                if (StringUtils.hasLength(value)) {
                    authUrl.append('=').append(formEncode(value));
                }
            }
        }
        return authUrl.toString();
    }

    private String formEncode(String data) {
        try {
            return URLEncoder.encode(data, StandardCharsets.UTF_8.name());
        }
        catch (UnsupportedEncodingException ex) {
            // should not happen, UTF-8 is always supported
            throw new IllegalStateException(ex);
        }
    }

    @Override
    public String requestForOrRenewAccessToken() {
        if (lastAccessTokenReqTime != 0 && System.currentTimeMillis() - lastAccessTokenReqTime < TIMEOUT_ACCESS_TOKEN) {
            return accessToken;
        }
        final OapiSnsGettokenRequest tokenRequest = new OapiSnsGettokenRequest();
        tokenRequest.setAppid(appId);
        tokenRequest.setAppsecret(appSecret);
        final DefaultDingTalkClient dingTalkClient = new DefaultDingTalkClient(URIBuilder.fromUri(accessTokenUrl).build().toString());
        try {
            final OapiSnsGettokenResponse getTokenResponse = dingTalkClient.execute(tokenRequest);
            accessToken = getTokenResponse.getAccessToken();
        } catch (com.taobao.api.ApiException e) {
            throw new ApiException("dingtalk", "Failed to request for access token", e);
        }
        lastAccessTokenReqTime = System.currentTimeMillis();
        return accessToken;
    }
}
