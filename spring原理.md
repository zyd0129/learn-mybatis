# spring ioc

## spring-mybatis中间件

### @MapperScan原理

#### MapperFactoryBean

```java
<bean class="org.mybatis.spring.mapper.MapperFactoryBean">
        <property name="mapperInterface" ref="com.ps.judge.dao.mapper.UserMapper"/>
        <property name="sqlSessionFactory" ref="sqlSessionFactory"/>
    </bean>
```



注入applicationContext

1.实现ApplicationContextAware接口

# Spring Security

spring security 时基于servlet filter实现

![filterchain](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/images/servlet/architecture/filterchain.png)

## DelegatingFilterProxy，FilterChainProxy，SecurityFilterChain，Filter

这个filter是按照servlet机制，注册到servlet filterChain里。  allows bridging between the Servlet container’s lifecycle and Spring’s `ApplicationContext`

DelegatingFilterProxy委托给FilterChainProxy，这个是一个spring bean， FilterChainProxy包含多个SecurityFilterChain，大部分情况下默认只有一个，针对每一个请求，都会选择一个SecurityFilterChain，针对spring security默认，由15个filter，就是放在SecurityFilterChain里的，FilterChainProxy把他们找出来，构建了一个VirtualFilterChain，先执行完15个filter，再执行servlet原本filterChain剩下的filter.

[`SecurityFilterChain`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/SecurityFilterChain.html) is used by [FilterChainProxy](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#servlet-filterchainproxy) to determine which Spring Security `Filter`s should be invoked for this request.

## Handling Security Exceptions

UsernamePasswordAuthenticationFilter 这个认证不成功 抛出AuthenticationException，但是被它的父类AbstractAuthenticationProcessingFilter捕获，通过failureHandler.onAuthenticationFailure(request, response, failed); 处理。 如果成功，则successfulAuthentication(request, response, chain, authResult); 就直接返还了，没有往下走，VirtualFilterChain剩下的filter就没执行，然后也servlet的原始filterChain也不往下走了，直接返还

那么ExceptionTranslationFilter 是在什么时候执行呢？

原来，servlet 监听被过滤器拦截了情况下，又请求了/error这个url



```java
OncePerRequestFilter 不去过滤错误 
private boolean skipDispatch(HttpServletRequest request) {
		if (isAsyncDispatch(request) && shouldNotFilterAsyncDispatch()) {
			return true;
		}
     //ERROR_REQUEST_URI_ATTRIBUTE 为出错前访问的页面
		if (request.getAttribute(WebUtils.ERROR_REQUEST_URI_ATTRIBUTE) != null && shouldNotFilterErrorDispatch()) {
			return true;
		}
		return false;
	}
```



ExceptionTranslationFilter 捕获的是FilterSecurityInterceptor的异常

异常分为两类：AccessDeniedException和 AuthenticationException



问题：什么时候是AuthenticationException？

## FilterSecurityInterceptor

The [`FilterSecurityInterceptor`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/access/intercept/FilterSecurityInterceptor.html) provides [authorization](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#servlet-authorization) for `HttpServletRequest`s



spring提供两种拦截方式

- **Web拦截**：HttpSecurity对Web进行安全配置，内置了大量GenericFilterBean过滤器对URL进行拦截。负责认证的过滤器会通过AuthenticationManager进行认证，并将认证结果保存到SecurityContext。
- **方法拦截**：Spring通过AOP技术（cglib/aspectj）对标记为@PreAuthorize、@PreFilter、@PostAuthorize、@PostFilter等注解的方法进行拦截，通过AbstractSecurityInterceptor调用AuthenticationManager进行身份认证（如果必要的话）。