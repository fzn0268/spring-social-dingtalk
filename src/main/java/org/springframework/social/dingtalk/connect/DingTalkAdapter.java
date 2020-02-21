package org.springframework.social.dingtalk.connect;

import com.dingtalk.api.response.OapiSnsGetuserinfoResponse;
import org.springframework.social.ApiException;
import org.springframework.social.connect.ApiAdapter;
import org.springframework.social.connect.ConnectionValues;
import org.springframework.social.connect.UserProfile;
import org.springframework.social.connect.UserProfileBuilder;
import org.springframework.social.dingtalk.api.DingTalk;

public class DingTalkAdapter implements ApiAdapter<DingTalk> {
    @Override
    public boolean test(DingTalk api) {
        try {
            api.userOperations().getUserInfo();
            return true;
        } catch (ApiException e) {
            return false;
        }
    }

    @Override
    public void setConnectionValues(DingTalk api, ConnectionValues values) {
        final OapiSnsGetuserinfoResponse resp = api.userOperations().getUserInfo();
        final OapiSnsGetuserinfoResponse.UserInfo userInfo = resp.getUserInfo();
        values.setProviderUserId(userInfo.getOpenid());
        values.setDisplayName(userInfo.getNick());
    }

    @Override
    public UserProfile fetchUserProfile(DingTalk api) {
        final OapiSnsGetuserinfoResponse resp = api.userOperations().getUserInfo();
        final OapiSnsGetuserinfoResponse.UserInfo userInfo = resp.getUserInfo();
        return new UserProfileBuilder()
                .setId(userInfo.getUnionid())
                .setUsername(userInfo.getDingId())
                .setName(userInfo.getNick()).build();
    }

    @Override
    public void updateStatus(DingTalk api, String message) {

    }
}
