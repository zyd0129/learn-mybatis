package com.learn.authserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.approval.ApprovalStore;
import org.springframework.security.oauth2.provider.approval.JdbcApprovalStore;
import org.springframework.security.oauth2.provider.client.JdbcClientDetailsService;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    DataSource dataSource;

    @Bean
    public JdbcClientDetailsService jdbcClientDetailsService() {
        //这个自带缓存功能
        return new JdbcClientDetailsService(dataSource);
    }

    /**
     * 配置客户端,
     *
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.withClientDetails(jdbcClientDetailsService());

//        clients.inMemory().withClient("c1")
//                .secret(passwordEncoder.encode("secret"))
//                .resourceIds("res1") //资源
//                .authorizedGrantTypes("authorization_code", "password", "client_credentials","implicit","refresh_token")
//                //这里没有常量，是因为这个是要读库的
//                .scopes("all")//允许的授权范围
//                .autoApprove(false)
////                .authorities()  //用来配合scope,如果scope不够用的话
//                .redirectUris("http://www.baidu.com");

    }

    /**
     * 配置端点权限,即配置另一条过滤器链的权限，这条过滤器链拦截 /oauth/token，/oauth/token_key，/oauth/check_token
     *
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.tokenKeyAccess("permitAll()")//公有密钥访问端点
//                .checkTokenAccess("isAuthenticated()")
        security.allowFormAuthenticationForClients(); //容许表单提交，默认是basic
        security.checkTokenAccess("permitAll()");
    }


    //     密码模式，用于用户认证
    @Autowired
    AuthenticationManager authenticationManager;

    // token存储策略
    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    //授权模式，用户授权客户端记录
    @Bean
    public ApprovalStore approvalStore() {
        return new JdbcApprovalStore(dataSource);
    }

    /**
     * 授权模式，code被使用之后，被从数据库删除
     *
     * @return
     */
    @Bean
    public AuthorizationCodeServices authorizationCodeServices() {
        return new JdbcAuthorizationCodeServices(dataSource);
    }
////    @Bean
////    public AuthorizationServerTokenServices tokenServices() throws Exception {
////        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
////        defaultTokenServices.setClientDetailsService(clientDetailsService());
////        defaultTokenServices.setTokenStore(tokenStore);
////        defaultTokenServices.setSupportRefreshToken(true);
////        defaultTokenServices.setRefreshTokenValiditySeconds(3600*10);
////        defaultTokenServices.setAccessTokenValiditySeconds(3600);
////        return defaultTokenServices;
////    }

    /**
     * 配置端点服务
     *
     * @param endpoints
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {

        endpoints.authenticationManager(authenticationManager) //密码模式需要
//                .tokenServices() //令牌服务
                .tokenStore(tokenStore())
                .approvalStore(approvalStore())
                .authorizationCodeServices(authorizationCodeServices())//授权码模式需要
        ;

    }


}
