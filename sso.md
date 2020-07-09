SSO  sigle-sign-on

单点登录，指公司旗下有多个网站，只需一次登录，就可以访问不同的网站，而不需要每个网站都要登录一遍。登录一次就可以访问相互信任的应用。免登录就是单点登录

常见解决方案

1.Central Authentication Service，基于session-cookie 但是不能提供三方登录，token一般存在redis或者mysql中实现集群认证服务器

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