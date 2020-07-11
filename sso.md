SSO  sigle-sign-on

单点登录，指公司旗下有多个网站，只需一次登录，就可以访问不同的网站，而不需要每个网站都要登录一遍。登录一次就可以访问相互信任的应用。免登录就是单点登录

常见解决方案

1.Central Authentication Service，基于session-cookie 但是不能提供三方登录，token一般存在redis或者mysql中实现集群认证服务器。 认证服务器必须基于cookie。

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



认证方式：

基于session: 需要客户端支持cookie, 服务端存储

基于token:客户端自定义存储，服务端可以不存储，如jwt token



给用户分配权限，为了方便分配，使用角色,  用户 角色 权限 之间都是多对多关系

授权模型： 基于角色，基于权限（推荐），因为权限基本是不变的，角色可以修改。

最原始的认证授权：session+interceptor



filter链 + authenticationManager + AccessDecisionManager



![image-20200709172243666](D:\learn\learn-mybatis\img\mybatis.md)



Authentication 认证信息，UsernameAndPasswordAuthenticationToken是其一个实现类

UserDetails 用户信息， User是其一个实现类

BCrypt 加盐hash，避免hash撞库

哈希表碰撞攻击就是通过精心构造数据，使得所有数据全部碰撞，人为将哈希表变成一个退化的单链表，此时哈希表各种操作的时间均提升了一个数量级，因此会消耗大量CPU资源，导致系统无法快速响应请求，从而达到拒绝服务攻击（DoS）的目的。



###### 为什么要加盐

我们从暴力破解说起，面对一个md5加密的密文，你会考虑这么破解。可以跑密码字典，也可以用查表法，包括反向查表，**彩虹表**之类的。其实本质都是暴力破解，只不过现场跑密码字典很慢，而用查表的话，特别是表的数据已经累计到一定程度之后，很可能一查一个准，就有点类似我们的缓存。所以我们需要加盐，即使通过一定的手段得到了明文，在不知道盐的情况下，也会增加一定的破解负担。



![image-20200709174356438](D:\learn\learn-mybatis\img\image-20200709174356438.png)

默认是Affirm 投票机制，只要有一个投票成功就通过。hasAnyAuthority('role_add','role_modify')，只要有一个就可以

CRSF

CORS

统一认证：要支持手机登录 扫码登录等 对内 对外

![image-20200709185105142](D:\learn\learn-mybatis\img\image-20200709185105142.png)

网关负责客户端权限校验，微服务负责用户权限校验

基于session, 有会话粘体 会话复制 会话存储等问题

oauth2 角色：用户（资源拥有者）、客户端、资源、授权服务器





令牌管理

InMemoryTokenStore,JdbcTokenStore,JWTTokenStore

访问端点

![image-20200709195258157](D:\learn\learn-mybatis\img\image-20200709195258157.png)



授权码模式:

/authroize 获取 授权码

/token获取令牌



开放接口，对第三方使用，采用网关？？ 开放接口和内部接口最好分开  /public



Oauth2 与sso的区别

假设有个系统A，B

sso 是指 用户在A登录了，用户在B操作时也登录了，都是用户在操作

oauth2是A系统 操作B系统，用户只在A系统操作。

![image-20200709233826409](/Users/yudong/learn/alibaba/java/imgs/image-20200709233826409.png)

sso是用户操作A，操作B

oauth是用户操作A，A操作B



oauth四种模式

授权模式 用户授权  返还授权码，服务端 拿着 授权码 去获取 token，用token获取资源

password  是用户提供密码给客户端，客户端问授权服务器，要token

简化模式 是对授权模式的简化，授权服务器不返还授权码，直接返还token，token直接暴露在外，有一定不安全

客户端模式，不需要用户参与，客户端凭借client_id client_secret直接获取token，获取资源



spring-oauth jwt 配置

```
encrypt:
  key-store:
    location: application.yml
    secret: 加盐
    password: 密码 //密钥库访问密码
    alias: 密钥别名  #一个keyStore里有多个公钥私钥对
```

jwt原理：token需要校验，传统方式需要请求到认证服务器进行校验，而jwt通过加解密来进行验证，只要资源服务器能解密就可以，不需要请求认证服务器了。这里要防止别人伪造令牌，所以加密凭证要保证安全，两种方式：

对称加密：认证服务器和资源服务器保存相同的secrete

非对称加密：RSA，认证服务器保存私钥，私钥加密（私钥也可以用来解密） 资源服务器保存公钥，公钥解密

生成密钥之后，是有RSA算法，生成token; 生成token的算法 应该要和生成密钥的算法一样，最起码支持



密钥生成工具 Keytool  公钥导出工具 openSSL



验证码登录实现方案：

1.session  验证码存在session里

2.无session，对验证码加密，讲验证码密文 和 图片 一起发送给客户端， 客户端提交的时候 需要提交验证码密码+验证码；服务端对密文解密后 和验证码对比

定义一个filter,注册在UsernameAndP...之前，因为时配合UsernameAndP..所以要配置只拦截 /login POST方法



短信验证码：

1.session 验证码存在session

2.验证码存在库里， 根据mobile查询

可以使用filter实现，也可通过provider实现，类似UsernameAndP..Token  AuthenticationManager AuthenticationProvider

安全过滤器链上的filter不要使用注解，要手动生成；否则会在原生过滤器链上 额外添加了一个过滤器。



jwt令牌结构，json结构

header + payload + 签名

payload设计：1.包含用户认证信息 2.其他辅助信息，如expiredTime等

jwt 是对摘要加密，对称加密，生成hash摘要，需要一个盐，也就是secret



jwt安全考虑

使用https，避免网络层面避免jwt泄露，jwt是明文的，header,payload只是简单base64加密

如果使用secret对称加密，要定期换密码；//这个对程序员是透明的，如果程序员泄露了，就完了

或者使用非对称加密 //只有开发oauth的知道

怎么防内部泄露？



jwt认证流程，生成jwt token

jwt token 授权流程

jwt token 为什么要支持刷新，token过期之后，用户要重新登录，再一次输密码，为了提高用户体验，所以要刷新。

刷新令牌：根据旧的令牌 生成新的令牌



jwt问题：

1.jwt对应的信息被修改，无法即使响应，老的jwt还是旧的信息

2.注销问题，特别针对单点登录，只要jwt未过期，就还能正常访问

解决方案：

1.维护一个redis，认证的时候查询下redis是否存在改jwt token，jwt被注销或修改，则从redis里删除，但这样失去了快速校验 Token 的优点。

2.多采用短期令牌，比如令牌有效期是 20 分钟，这样可以一定程度上降低注销后 Token 可用性的风险

3.过期问题，就要去刷新令牌，如果监测到token过期，就要根据refresh_token去刷新,注销的时候要把refresh_token干掉；这个机制也比较负责，建议直接使用

spring-oauth spring-oaut-sso

由于sso是基于cookie,后台使用了session。如果为了实现oauth集群，需要使用redis-session。



跨域访问(CORS)解决方案

1.使用nigix 代理 吧前端和后端都放在代理后 这样就同源了



基于oauth2 jwt实现SSO, 核心原理认证服务器仍然是基于cookie的, 只不过在验证token的时候，没有向认证服务器端请求验证，获取用户信息（状态，session），可以说是一种无状态会话

1.使用授权码模式，不使用密码模式，如果使用密码模式，在不同应用，都需要客户提交用户名和密码，就不是单点登录了

2.autoApprove(true)

3.为了简便开发，使用@EnableOAuth2Sso



有状态会话： cookie+sesssion  或者 token+session

无状态会话：只有token

也就是说服务器有无session，就是有无状态

无状态就是 会话信息只存在客户端



网关与权限

1.如果网关做统一权限，内部服务之间调用怎么控制权限？ url作为资源，一个权限对应一个url。这里必须要求所有微服务之间调用都通过网关，禁止直接调用

2.在网关层负责token解析，认证，不做权限控制，用户信息放在header里，从微服务出去的请求也带上权限信息



在微服务具体开发过程中，注册/登录可能是另一个微服务提供的功能，需要后台restTemplate远程调用oauth服务。或者由认证服务器提供注册功能，或者单点登录系统。。。。。很多情况，具体要看架构设计。