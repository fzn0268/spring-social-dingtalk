package org.springframework.social.dingtalk.api;

import com.dingtalk.api.response.OapiSnsGetuserinfoResponse;

public interface UserOperations {
    OapiSnsGetuserinfoResponse getUserInfo();
}
