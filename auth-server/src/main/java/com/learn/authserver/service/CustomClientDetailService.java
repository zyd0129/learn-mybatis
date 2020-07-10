package com.learn.authserver.service;

import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.ClientRegistrationException;
import org.springframework.security.oauth2.provider.client.BaseClientDetails;

import java.util.Arrays;
import java.util.Collections;

//这里也可以自定义，或者直接使用oauth自带的 jdbcClient...设置下数据源即可

public class CustomClientDetailService implements ClientDetailsService {
    @Override
    public ClientDetails loadClientByClientId(String clientId) throws ClientRegistrationException {

        BaseClientDetails clientDetails = new BaseClientDetails();
        clientDetails.setClientId("c1");
        clientDetails.setClientSecret("$2a$10$5NDp62dc6vO/nem1NnKnNuvCbRu9h3nrJtWQAl5hTfltGtJ1nSqEy");
        clientDetails.setResourceIds(Collections.singletonList("res1"));
        clientDetails.setScope(Collections.singletonList("all"));
        clientDetails.setRegisteredRedirectUri(Collections.singleton("http://www.baidu.com"));
        return  clientDetails;
    }
}
