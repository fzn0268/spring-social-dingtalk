package org.springframework.social.dingtalk.api.impl;

import com.dingtalk.api.request.OapiSnsGetPersistentCodeRequest;
import com.dingtalk.api.request.OapiSnsGettokenRequest;
import com.dingtalk.api.response.OapiSnsGetPersistentCodeResponse;
import com.dingtalk.api.response.OapiSnsGettokenResponse;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.social.dingtalk.api.DingTalkOAuth2Operations;
import org.springframework.social.dingtalk.util.DingTalkApiUriUtil;
import org.springframework.social.oauth2.*;
import org.springframework.social.support.ClientHttpRequestFactorySelector;
import org.springframework.social.support.FormMapHttpMessageConverter;
import org.springframework.social.support.LoggingErrorHandler;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class DingTalkOAuth2Template implements DingTalkOAuth2Operations {
    private static final long TIMEOUT_ACCESS_TOKEN = TimeUnit.HOURS.toMillis(2);

    private final String appId;

    private final String appSecret;

    private final String accessTokenUrl;

    private volatile String accessToken;

    private volatile long lastAccessTokenReqTime;

    private final String getPersistentCodeUrl;

    private final String authorizeUrl;

    private String authenticateUrl;

    private RestTemplate restTemplate;

    /**
     * Constructs an OAuth2Template for a given set of client credentials.
     * Assumes that the authorization URL is the same as the authentication URL.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token.
     */
    public DingTalkOAuth2Template(String appId, String appSecret, String authorizeUrl, String accessTokenUrl, String getPersistentCodeUrl) {
        this(appId, appSecret, authorizeUrl, null, accessTokenUrl, getPersistentCodeUrl);
    }

    /**
     * Constructs an OAuth2Template for a given set of client credentials.
     * @param appId the client ID
     * @param appSecret the client secret
     * @param authorizeUrl the base URL to redirect to when doing authorization code or implicit grant authorization
     * @param authenticateUrl the URL to redirect to when doing authentication via authorization code grant
     * @param accessTokenUrl the URL at which an authorization code, refresh token, or user credentials may be exchanged for an access token
     */
    public DingTalkOAuth2Template(String appId, String appSecret, String authorizeUrl, String authenticateUrl, String accessTokenUrl, String getPersistentCodeUrl) {
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
    }

    /**
     * Set the request factory on the underlying RestTemplate.
     * This can be used to plug in a different HttpClient to do things like configure custom SSL settings.
     * @param requestFactory the request factory used by the underlying RestTemplate
     */
    public void setRequestFactory(ClientHttpRequestFactory requestFactory) {
        Assert.notNull(requestFactory, "The requestFactory property cannot be null");
        getRestTemplate().setRequestFactory(requestFactory);
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
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
//        params.set("appid", appId);
//        params.set("appsecret", appSecret);
//        params.set("tmp_auth_code", authorizationCode);
//        params.set("redirect_uri", redirectUri);
//        params.set("grant_type", "authorization_code");
//        if (additionalParameters != null) {
//            params.putAll(additionalParameters);
//        }
        final OapiSnsGetPersistentCodeRequest persistentCodeRequest = new OapiSnsGetPersistentCodeRequest();
        persistentCodeRequest.setTmpAuthCode(authorizationCode);
        for (Map.Entry<String, String> entry : persistentCodeRequest.getTextParams().entrySet()) {
            params.add(entry.getKey(), entry.getValue());
        }
        return postForAccessGrant(getPersistentCodeUrl, params);
    }


    public AccessGrant exchangeCredentialsForAccess(String username, String password, MultiValueMap<String, String> additionalParameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.set("appid", appId);
        params.set("appsecret", appSecret);
        params.set("username", username);
        params.set("password", password);
        params.set("grant_type", "password");
        if (additionalParameters != null) {
            params.putAll(additionalParameters);
        }
        return postForAccessGrant(getPersistentCodeUrl, params);
    }

    @Deprecated
    public AccessGrant refreshAccess(String refreshToken, String scope, MultiValueMap<String, String> additionalParameters) {
        additionalParameters.set("scope", scope);
        return refreshAccess(refreshToken, additionalParameters);
    }

    public AccessGrant refreshAccess(String refreshToken, MultiValueMap<String, String> additionalParameters) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.set("appid", appId);
        params.set("appsecret", appSecret);
        params.set("refresh_token", refreshToken);
        params.set("grant_type", "refresh_token");
        if (additionalParameters != null) {
            params.putAll(additionalParameters);
        }
        return postForAccessGrant(getPersistentCodeUrl, params);
    }

    public AccessGrant authenticateClient() {
        return authenticateClient(null);
    }

    public AccessGrant authenticateClient(String scope) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<String, String>();
        params.set("appid", appId);
        params.set("appsecret", appSecret);
        params.set("grant_type", "client_credentials");
        if (scope != null) {
            params.set("scope", scope);
        }
        return postForAccessGrant(getPersistentCodeUrl, params);
    }

    // subclassing hooks

    /**
     * Creates the {@link RestTemplate} used to communicate with the provider's OAuth 2 API.
     * This implementation creates a RestTemplate with a minimal set of HTTP message converters ({@link FormHttpMessageConverter} and {@link MappingJackson2HttpMessageConverter}).
     * May be overridden to customize how the RestTemplate is created.
     * For example, if the provider returns data in some format other than JSON for form-encoded, you might override to register an appropriate message converter.
     * @return a {@link RestTemplate} used to communicate with the provider's OAuth 2 API
     */
    protected RestTemplate createRestTemplate() {
        ClientHttpRequestFactory requestFactory = ClientHttpRequestFactorySelector.getRequestFactory();
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        List<HttpMessageConverter<?>> converters = new ArrayList<HttpMessageConverter<?>>(2);
        converters.add(new FormHttpMessageConverter());
        converters.add(new FormMapHttpMessageConverter());
        converters.add(new MappingJackson2HttpMessageConverter(Jackson2ObjectMapperBuilder.json().propertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE).build()));
        restTemplate.setMessageConverters(converters);
        restTemplate.setErrorHandler(new LoggingErrorHandler());
        return restTemplate;
    }

    /**
     * Posts the request for an access grant to the provider.
     * The default implementation uses RestTemplate to request the access token and expects a JSON response to be bound to a Map. The information in the Map will be used to create an {@link AccessGrant}.
     * Since the OAuth 2 specification indicates that an access token response should be in JSON format, there's often no need to override this method.
     * If all you need to do is capture provider-specific data in the response, you should override createAccessGrant() instead.
     * However, in the event of a provider whose access token response is non-JSON, you may need to override this method to request that the response be bound to something other than a Map.
     * For example, if the access token response is given as form-encoded, this method should be overridden to call RestTemplate.postForObject() asking for the response to be bound to a MultiValueMap (whose contents can then be used to create an AccessGrant).
     * @param getPersistentCodeUrl the URL of the provider's access token endpoint.
     * @param parameters the parameters to post to the access token endpoint.
     * @return the access grant.
     */
    @SuppressWarnings("unchecked")
    protected AccessGrant postForAccessGrant(String getPersistentCodeUrl, MultiValueMap<String, String> parameters) {
        requestForOrRenewAccessToken();
        final Map<String, String> queryParams = new HashMap<>();
        queryParams.put("access_token", accessToken);
        return extractAccessGrant(getRestTemplate().postForObject(getPersistentCodeUrl, parameters, OapiSnsGetPersistentCodeResponse.class, queryParams));
    }

    protected AccessGrant createAccessGrant(OapiSnsGetPersistentCodeResponse persistentCodeResponse) {
        return new AccessGrant(persistentCodeResponse.getOpenid() + ":" + persistentCodeResponse.getUnionid() + ":" + persistentCodeResponse.getPersistentCode());
    }

    // testing hooks

    protected RestTemplate getRestTemplate() {
        // Lazily create RestTemplate to make sure all parameters have had a chance to be set.
        // Can't do this InitializingBean.afterPropertiesSet() because it will often be created directly and not as a bean.
        if (restTemplate == null) {
            restTemplate = createRestTemplate();
        }
        return restTemplate;
    }

    // internal helpers

    private String buildAuthUrl(String baseAuthUrl, GrantType grantType, OAuth2Parameters parameters) {
        StringBuilder authUrl = new StringBuilder(baseAuthUrl);
        if (grantType == GrantType.AUTHORIZATION_CODE) {
            authUrl.append('&').append("response_type").append('=').append("code");
        } else if (grantType == GrantType.IMPLICIT_GRANT) {
            authUrl.append('&').append("response_type").append('=').append("token");
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

    private AccessGrant extractAccessGrant(OapiSnsGetPersistentCodeResponse resp) {
        return createAccessGrant(resp);
    }

    @Override
    public void requestForOrRenewAccessToken() {
        if (lastAccessTokenReqTime != 0 && System.currentTimeMillis() - lastAccessTokenReqTime < TIMEOUT_ACCESS_TOKEN) {
            return;
        }
        final OapiSnsGettokenRequest getTokenRequest = new OapiSnsGettokenRequest();
        getTokenRequest.setAppid(appId);
        getTokenRequest.setAppsecret(appSecret);
        final OapiSnsGettokenResponse getTokenResponse = getRestTemplate().getForObject(accessTokenUrl, OapiSnsGettokenResponse.class, getTokenRequest.getTextParams());
        accessToken = getTokenResponse.getAccessToken();
        lastAccessTokenReqTime = System.currentTimeMillis();
    }
}
