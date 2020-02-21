package org.springframework.social.dingtalk.api;

import org.springframework.social.ApiBinding;

public interface DingTalk extends ApiBinding {
    String requestForOrRenewSnsToken();

    UserOperations userOperations();
}
