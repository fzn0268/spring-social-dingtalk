package org.springframework.social.dingtalk.api;

import com.dingtalk.api.request.OapiSnsGetuserinfoBycodeRequest;
import com.dingtalk.api.response.OapiSnsGetuserinfoBycodeResponse;
import com.dingtalk.api.response.OapiSnsGetuserinfoResponse;

public interface UserOperations {
    OapiSnsGetuserinfoBycodeResponse getUserInfo(OapiSnsGetuserinfoBycodeRequest userGetRequest);

    OapiSnsGetuserinfoResponse getUserInfo();
}
