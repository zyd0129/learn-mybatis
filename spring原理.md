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



# Spring MVC

## Filter

创建方式，spring会在servlet的过滤器链里添加一个filter,这个filter是和spring security的DelegatingFilterProxy同级别的。

```java
@Component
public class MyFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("enter myFilter");
        chain.doFilter(request, response);
        System.out.println("leave myFilter");
    }
}
```

```java
@Bean
    public FilterRegistrationBean registrationBean(){
        FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MyFilter("hello"));
        registration.setUrlPatterns(Collections.singletonList("/*"));
        return registration;
    }
```

**实测 FilterRegistrationBean会覆盖同名的filter，这个需要注意。**

谁说在filter里不可以注入bean？

以上两种方式都是创建filter,都是spring bean,所以当然可以注入。那他们说的不可以注入 是针对什么情况呢?

```
这是针对在非springboot下，直接使用xml配置filter，或者使用@webFilter注解，这种方式，这些filter都是有servlet container维护的，不是spring bean
所以不能注入
```

在springboot中，单独使用@webFitler 不生效，需要结合@ServletComponentScan注解。推荐使用前两种方式。



# Spring Security

spring security 时基于servlet filter实现

![filterchain](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/images/servlet/architecture/filterchain.png)

## DelegatingFilterProxy，FilterChainProxy，SecurityFilterChain，Filter

这个filter是按照servlet机制，注册到servlet filterChain里。  allows bridging between the Servlet container’s lifecycle and Spring’s `ApplicationContext`

DelegatingFilterProxy委托给FilterChainProxy，这个是一个spring bean， FilterChainProxy包含多个SecurityFilterChain，大部分情况下默认只有一个，针对每一个请求，都会选择一个SecurityFilterChain，针对spring security默认，由15个filter，就是放在SecurityFilterChain里的，FilterChainProxy把他们找出来，构建了一个VirtualFilterChain，先执行完15个filter，再执行servlet原本filterChain剩下的filter.

[`SecurityFilterChain`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/SecurityFilterChain.html) is used by [FilterChainProxy](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#servlet-filterchainproxy) to determine which Spring Security `Filter`s should be invoked for this request.

## Handling Security Exceptions

UsernamePasswordAuthenticationFilter  如果成功，则successfulAuthentication(request, response, chain, authResult);  这个认证不成功 抛出AuthenticationException，但是被它的父类AbstractAuthenticationProcessingFilter捕获，通过failureHandler.onAuthenticationFailure(request, response, failed); 处理。就直接返还了，没有往下走，VirtualFilterChain剩下的filter就没执行，然后也servlet的原始filterChain也不往下走了，直接返还

那么ExceptionTranslationFilter 是在什么时候执行呢？

原来，onAuthenticationFailure 中 调用了sendError, 转到 /error请求了，又重走了一遍过滤器链，但是有的过滤器针对这次请求就没有过滤。



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



## ExceptionTranslationFilter 

捕获的是FilterSecurityInterceptor的异常

异常分为两类：AccessDeniedException和 AuthenticationException



但是basic-auth 如果密码不对 没有走到BasicErrorController，是因为/error被设置成了认证了才能访问，这就又转到sendError了，但是这次却不能访问到sendError了。即一次完整的请求中sendError只有一次会被转到ErrorController,第二次不会了

直接访问error页面，第一次不会，第二次会走到BasicErrorController

问什么情况下才会走到BasicErrorController？

不带认证信息，先是请求原始Url，被FilterSecurityInterceptor授权拦截，之后转向/error，这次FilterSecurityInterceptor没有拦截，直接找到BasicErrorController

带认证信息，在basicAuth 停止往下，...，转向/error，这次basic没有拦截（只调用一次），走向ExeceptionHandler,但是被FilterSecurityInterceptor授权拦截抛出异常，ExeceptionHandler捕获处理,sendError,但是这次却直接返还了，没有走到BasicErrorController。 因为我们/error也加了权限

```java
response.sendError //这个会转向 /error
同时，会清空filter，除了DelegatingFilterProxy,即Security filter chain;用以保证filter只会被执行一次,sendError不能算是第二次请求，只是内部转向
所以如果想在ThreadLocal里保存seqId,一定要在delegateFilteProxy之前定义filter，所以必须指定order，order越小，越先执行，在老版本中可能不生效
```

问题：什么时候是AuthenticationException？

ExceptionTranslationFilter默认authenticationEntryPoint BasicAuthenticationEntryPoint 

无认证信息 访问需要认证的页面  FilterSecurityInterceptor拦截  AccessDeniedException  匿名用户 转到authenticationEntryPoint   sendError FilterSecurityInterceptor不再拦截   errorController返还 401 Unauthorized

认证用户  。。。（同上）。。。 AccessDeniedException  认证用户 转到authenticationEntryPoint  。。。（同上）。。。 返还403 Forbidden



认证出错，几种处理方法

1. 浏览器，转到登录页面，
2. 直接返还错误信息
3. 抛出异常，转到/error处理（这里/error不能加认证权限访问)

我们再来分析下 方法层面的权限控制，这里如果设置了一下，那么异常就在这里处理了，没有转到/error里

```java
@RestControllerAdvice
public class GlobalAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public void handlerAccessDeniedException(AccessDeniedException ex) {
        System.out.println(ex);
    }
}

```

但是RestControllerAdvice只能捕获 controller层异常，对于filter内部的异常却无法捕获。

综上的话，如果自定义异常，使用ErrorController时比较合适的选择。

## FilterSecurityInterceptor

The [`FilterSecurityInterceptor`](https://docs.spring.io/spring-security/site/docs/current/api/org/springframework/security/web/access/intercept/FilterSecurityInterceptor.html) provides [authorization](https://docs.spring.io/spring-security/site/docs/5.3.2.RELEASE/reference/html5/#servlet-authorization) for `HttpServletRequest`s



spring提供两种拦截方式

- **Web拦截**：HttpSecurity对Web进行安全配置，内置了大量GenericFilterBean过滤器对URL进行拦截。负责认证的过滤器会通过AuthenticationManager进行认证，并将认证结果保存到SecurityContext。
- **方法拦截**：Spring通过AOP技术（cglib/aspectj）对标记为@PreAuthorize、@PreFilter、@PostAuthorize、@PostFilter等注解的方法进行拦截，通过AbstractSecurityInterceptor调用AuthenticationManager进行身份认证（如果必要的话）。