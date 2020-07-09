SSO  sigle-sign-on

单点登录，指公司旗下有多个网站，只需一次登录，就可以访问不同的网站，而不需要每个网站都要登录一遍。

常见解决方案

1.Central Authentication Service，基于session-cookie

登录流程：

1. 网站A  登录跳转到 认证中心 认证中心 使用cookie-session进行认证，成功之后返还ticket/token给网站A， A拿个token之后，还需要把token发送给认证中心，进行token验证，如果验证成功，则登录成功。这一步可以在filter/intercepter中实现。
2. 网站B  访问授权网站时，如果未登录，也跳转到认证中心，由于认证中心使用cookie-session，会话被保持，直接redirect到B网站，也把token返还给B，B也进行token验证，如果验证成功，则登录成功

以上时基于跳转+token验证的方式实现

B网站，也可通过jsonP的方式，访问认证中心，去拿到那个token。这同样利用了会话保持的功能。让后再进行token验证

认证中心，集群的时候，使用redis存储session即可

退出功能：一处退出，处处退出

1.让认证服务的session过期即可

2.监听session过期事件，然后发送请求通知所有子项目，让子项目的session过期，token失效即可

2. oauth



spring-security

异常自定义处理举例

UsernameAndPasswordFilter 提供successHandler，failureHandler扩展接口

```java
http.formLogin().successHandler().failureHandler()
```

ExceptionTranslationFilter 提供 认证处理和权限处理两个扩展接口

```java
http.exceptionHandling().authenticationEntryPoint().accessDeniedHandler()
```



remember-me原理

需要客户端cookie支持，将认证信息保存在了redis或数据库里了。 cookie存一个token标指，根据token标指去数据库里查询.



GlobalAdivice正确写法,需要加上@ResponseStatus

```java
@ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ApiResponse handle(AccessDeniedException e) {
        log.error(e.getMessage(), e);
        return ApiResponse.error(HttpStatus.FORBIDDEN.value(), e.getMessage());
    }
```

