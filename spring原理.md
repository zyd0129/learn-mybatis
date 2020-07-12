# spring iocs

## ioc与aop

什么是ioc？控制反转

不用手动创建对象和管理对象之间的关系。控制指对象的创建和之间的关系；反转是指有我们自己管理交给框架管理。

我想这也是beanFactory只提供一个get方法的原因，get屏蔽了对象的创建过程。对象交给容器创建了，我们只需要获取即可，所以只提供get方法。

DI 依赖注入

从容器的角度，维护对象间依赖关系。

依赖倒置

针对接口编程，而不是具体实现，借助DI，实现接口和具体实现类之间的耦合。 针对接口编程，new关键字会带来耦合问题。

```java
class IService {
IService a= new IServiceImpl();
}
```

aop

相同业务逻辑（执行流程）有相同部分（子流程）。这叫横切逻辑。业务逻辑执行流程是一种垂直逻辑。



## 手写ioc思路

1.去除new，通过反射机制，class.forName()实例化，可以把类限定名配置在xml中，也就说通过xml配置产生对象

2.通过反射技术实例化对象，生产对象就是可以定义一个工厂，工厂类解析xml，根据反射技术实例化对象

3.生产的对象要放在一个容器里存储



任务一： 解决new，通过xml反射生产

可以简单定义个一个工厂类

```java
class BeanFactory{
  private static Map<String,Object> = new HashMap<>();
  public static Object getBean(String id){}
  static {
    //这里直接利用dom4j解析xml, 根据反射机制生产对象，放入map
  }
}
```

任务二：维护依赖



## 事务

ACID 

原子性，一致性，隔离性，持久性

隔离性：

- 

spring的事务，就是数据库事务。

回滚：默认是只有发生RuntimeException时才回滚，可以通过设置rollbackFor属性

事务的传播机制：7种

required：默认，不存在创建，存在使用已存在的

new(挂起当前事务,

support

Not support

Mandatory

nest（外部回滚，会连代内部回滚),

never

# spring-mybatis中间件

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



但是注意：还有监听器

拦截调用链： filter->interceptor->controllerAdvice->aop

从整体上讲，返还异常处理由两种方式：1.转到某个controller 2.response.write()



多请求之间怎么共享登录？通过session.   SecurityContextPersistenceFilter是第二个过滤器，从session里取认证信息放到securityContext里，出去的时候，再把认证放到session里



# Spring Oauth2

四种授权模式模式



## 授权码（authorization_code)：

1.http://localhost:8080/oauth/authorize?client_id=c1&response_type=code&scope=all&redirect_url="http://www.baidu.com"

​	scope=all&redirect_url="http://www.baidu.com" 可以不用传，存在数据库里

2.用户登录

3.用户授权

4.跳转到redirect_url, 返还code

5.获取token

```
curl --location --request POST 'localhost:8080/oauth/token' \
--header 'Authorization: Basic YzE6c2VjcmV0' \
--header 'Cookie: JSESSIONID=5B5883E73DCA65C027B8C53DCD65F5BE' \
--form 'client_id=c1' \
--form 'client_secret=secret' \
--form 'grant_type=authorization_code' \
--form 'code=Gx6y0C'
```

6 check token http://localhost:8080/oauth/check_token?token=4983b202-5e14-44d3-9d75-7de1217c8eb7

```json
{
    "aud": [
        "res1"
    ],
    "user_name": "zyd",
    "scope": [
        "all"
    ],
    "active": true,
    "exp": 1594409435,
    "authorities": [
        "AMDIN"
    ],
    "client_id": "c1"
}
```

## 密码模式

密码模式，直接返还token

```java
curl --location --request POST 'localhost:8080/oauth/token' \
--header 'Authorization: Basic YzE6c2VjcmV0' \
--header 'Cookie: JSESSIONID=8BF669882968098C527D46A29A424680' \
--form 'client_id=c1' \
--form 'client_secret=123456' \
--form 'grant_type=password' \
--form 'username=admin' \
--form 'password=123456'
```

在spring中，必须配置authenticationManager，才能开启



oauth2原理

使用了两个过滤器链，oauth和普通认证链

```java
FilterChainProxy.class
List<SecurityFilterChain> filterChains
```

oauth过滤器链拦截 /oauth/token，/oauth/token_key，/oauth/check_token

这里针对client_id,client_secret进行认证，默认是basic认证，如果想要表单认证，需要做一下配置

```java
@Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.tokenKeyAccess("permitAll()")//公有密钥访问端点
//                .checkTokenAccess("permitAll()")
              security.allowFormAuthenticationForClients(); //容许表单提交，默认是basic
    }
```



最简单搭建方式

1.配置webSecurity

```java
@Configuration
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Bean
    public PasswordEncoder passwordEncoder() {
        // DelegatingPasswordEncoder 委托模式，或者是策略模式，可以动态决定 passwordEncode
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication().withUser("admin").password(passwordEncoder().encode("123456")).roles("ADMIN");
    }
}
```

2.配置AuthorizationServerConfig

```java
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig  extends AuthorizationServerConfigurerAdapter {

    @Autowired
    PasswordEncoder passwordEncoder;

    /**
     * 配置客户端
     * @param clients
     * @throws Exception
     */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
//        clients.withClientDetails(clientDetailsService());
        clients.inMemory().withClient("c1")
                .secret(passwordEncoder.encode("secret"))
                .resourceIds("res1") //资源
                .authorizedGrantTypes("authorization_code", "password", "client_credentials","implicit","refresh_token")
                //这里没有常量，是因为这个是要读库的
                .scopes("all")//允许的授权范围
                .autoApprove(false)
//                .authorities()  //用来配合scope,如果scope不够用的话
                .redirectUris("http://www.baidu.com");

    }
    /**
     * 配置端点权限,即配置另一条过滤器链的权限，这条过滤器链拦截 /oauth/token，/oauth/token_key，/oauth/check_token
     * @param security
     * @throws Exception
     */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
//        security.tokenKeyAccess("permitAll()")//公有密钥访问端点
//                .checkTokenAccess("permitAll()")
              security.allowFormAuthenticationForClients(); //容许表单提交，默认是basic
    }
}

```

```
client过滤器链 ClientCredentialsTokenEndpointFilter 只过滤 /oauth/token POST
user过滤器链 UsenameAndPass...Filter 只过滤 /login  POST
```