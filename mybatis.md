# Mybatis

传统jdbc缺点：1.没有连接池 2.sql和代码在一起，不便于维护，改sql就要重新编译 3.resultset 硬编码，模板代码

```java
public static void main(String[] args) throws Exception {
        //1.加载驱动程序
        Class.forName("com.mysql.jdbc.Driver");
        //2. 获得数据库连接
        Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
        //3.操作数据库，实现增删改查
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT user_name, age FROM imooc_goddess");
        //如果有数据，rs.next()返回true
        while(rs.next()){
            System.out.println(rs.getString("user_name")+" 年龄："+rs.getInt("age"));
        }
    }
```

ORM  Object-Relation Mapping

既然有了 SqlSessionFactory，顾名思义，我们可以从中获得 SqlSession 的实例。SqlSession 提供了在数据库执行 SQL 命令所需的所有方法。你可以通 SqlSession 实例来直接执行已映射的 SQL 语句

单纯使用jdbc,  需要有具体的dao层实现，使用mybatis，只需要接口，不需要实现，通过配置即可。底层是mybatis通过动态代理生成实现类。

jdbc方式，需要手动编写实现类

```java
public class UserDaoImpl implements UserMapper {
    public List<User> getUserList() {
        return null;
    }
}
```

mybatis方式，只需一个mapper配置

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<!--mapper通过namespace和dao关联-->
<mapper namespace="mybatis.learn.dao.mapper.UserMapper">
    <select id="getUserList" resultType="mybatis.learn.dao.entities.User">
    select * from Blog
  </select>
</mapper>
```

```
jdbc:mysql://localhost:3306/mybatis?useSSL=false&amp;useUnicode=true&amp;characterEncoding=utf-8
```

这里如果使用useSSL=true，报以下错误

```
### Error querying database.  Cause: com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
...
### Caused by: java.security.cert.CertPathValidatorException: Path does not chain with any of the trust anchors
```

mybatis 注解开发只适合于简单sql开发，对于复杂sql使用xml开发。

mybatis开发步骤

1. 定义全局配置文件 mybatis-config.xml，定义enviroments,typeAliase,properties,settings, mappers

2. 通过sqlSessionFactoryBuilder创建sqlSessionFactory, 全局唯一

3. 获取sqlSession,通过sqlSession获取mapper，进而操作数据库， 线程唯一

   ```java
    SqlSession sqlSession = MybatisUtils.getSqlSession();
   
           UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
   
           List<User> users = userMapper.getUserList();
           users.forEach(o -> System.out.println(o.toString()));
           sqlSession.close();
   ```

模糊查询

```sql
 select * from mybatis.user where name like "%"#{name}"%"
```



resultMap结果映射

```xml
<resultMap id="userMap" type="User">
        <result property="pwd" column="password"/>
    </resultMap>
```

参数，mapper调用，需要参数处理：

- 单个参数
- 多个参数 封装成map，按照0，1。。。或者 param1,param2... 如果使用@Param可以指定参数名
- Map
- 如果是List, 封装成map,key是list

源码

1. 先封装一个names, key:顺序下标，value是参数名，如果使用了@Param，则是Param名，如果不是则是顺序下标 ParamNameResolver构造函数，生成names

2. 将args封装成一个ParamMap，名称是names[Value],key是args[names[key]] ，另外增加了param1,param2...参数名

   getNamedParams方法生成

这样我们就可以在sql里使用参数名

想想两种调用方式

直接通过statement id,调用 sqlSession.selectList(String statement, Object parameter, RowBounds rowBounds);

通过mapper调用，我们就是把方法参数，转换成parameter，因为mapper代理对象也是最终调用sqlSession的方法。



枚举类型(默认类型)

![image-20200705155849957](/Users/yudong/learn/alibaba/java/imgs/image-20200705155849957.png)

默认是使用name

```java
System.out.println(StatusEnum.SUCCESS.ordinal());
System.out.println(StatusEnum.SUCCESS.name());
```

在配置文件里增加

<typeHandlers><typeHandler handler javaType>

自定义枚举类型

![image-20200705160256376](/Users/yudong/learn/alibaba/java/imgs/image-20200705160256376.png)

需要自定义typeHandler

![image-20200705160754078](/Users/yudong/learn/alibaba/java/imgs/image-20200705160754078.png)

对象

多表查询， 几种实现方式

1.多表查询，实体使用继承

2.多对一，使用组合关系,多表查询，使用associate

```xml
    <resultMap id="userMap" type="User">
        <association property="teacher" javaType="Teacher">
            <result property="id" column="tid"/>
            <result property="name" column="tname"/>
        </association>
    </resultMap>
    <select id="getUserList" resultMap="userMap">
    select u.*, t.id tid, t.name tname from mybatis.user as u, mybatis.teacher as t where u.tid=t.id
    </select>
```

3.多对一，使用组合关系，单表查询，使用associatie+select,相当于嵌套查询，会导致1+N问题，导致一次查询带出成千上百个查询，效率低。

4.一对多，使用List组合，可以使用多表查询collection，或者级联查询  collection+select

```xml
<resultMap id="teacherMap" type="Teacher">
        <collection property="students" ofType="User">
            <result property="id" column="uid"/>
            <result property="name" column="uname"/>
            <result property="tid" column="id"/>
        </collection>
    </resultMap>
```



动态sql, if,choose,foreach,trim(where,set)

```
<if test="title != null">
    AND title like #{title}
  </if>
```

where标签，*where* 元素只会在子元素返回任何内容的情况下才插入 “WHERE” 子句。而且，若子句的开头为 “AND” 或 “OR”，*where* 元素也会将它们去除。

where标签可以替换 1=1

```
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  <where>
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
  </where>
</select>
```

set标签，可以去除不必要的，

```java
<update id="updateAuthorIfNecessary">
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </set>
  where id=#{id}
</update>
```

trim标签，set和where本质可以用trim标签实现

```xml
<trim prefix="where" prefixOverrides="or | and">
            
        </trim>
```



这个用于带括号的where

```xml
<trim prefix="(" suffix=")" prefixOverrides="or | and">
            
        </trim>
```

这个用于insert

```xml
<trim prefix="(" suffix=")" suffixOverrides=",">
            
        </trim>
```

sql片段复用  <sql/><include/>

foreach

```java
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  WHERE ID in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item}
  </foreach>
</select>
```



缓存

默认开启一级缓存，同一个sqlSession下，查询相同，只会查一遍。 一级缓存，也叫本地缓存。增删改都会清理缓存，使用不同mapper 二级缓存只对同一个mapper有效

一级缓存缓存再sqlSession，二级缓存在mapper

使用自定义缓存，enhancer，enhancer需要一个配置文件

本地缓存 与 redis缓存区别？？

mysql 8 连接需要加时区 jdbc:mysql://192.168.40.147:3306/judge?useUnicode=true&characterEncoding=utf-8&useSSL=true&serverTimezone=UTC

mysql 8驱动 com.mysql.jdbc.Driver

mysql 5 驱动 com.mysql.cj.jdbc.Driver

# Mybatis Spring

使用步骤：

1. 系统自动配置了datasource, sqlsessionFactory

2. 使用@MapperScan注解，扫描接口，这里是注册Mapper，与基本mybatis不一样，通过配置文件mybatis-config.xml，mappers标签注册映射器

   如果在接口对应包下找到对应mapper文件，则使用；在接口里面也可使用注解，比如@Select；如果在mapper文件里 和接口里同时定义mappedStatement,会报错。建议只使用一种。

3. 如果mapper文件和接口不在同一包下，需要指定包的位置。 但这里不做Mapper注册，Mapper注册只在2发生。2的本质，是通过import标签，扫描指定包下的所有接口，创建对应的 MapperFactoryBean。 如果不使用@MapperScan，需要一个个注册

```xml
mybatis.mapper-locations=classpath:mapper/**/*.xml
```



```xml
<bean id="userMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
  <property name="mapperInterface" value="org.mybatis.spring.sample.mapper.UserMapper" />
  <property name="sqlSessionFactory" ref="sqlSessionFactory" />
</bean>

@Bean
public UserMapper userMapper() throws Exception {
  SqlSessionTemplate sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory());
  return sqlSessionTemplate.getMapper(UserMapper.class);
}
```

执行批量的时候，可以单独配置一个sqlSession,设置批量executor 在service里注入sqlsession手动获取mapper

# Mybatis Plus

- **强大的 CRUD 操作**：内置通用 Mapper、通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求. 直接继承BaseMap即可
- 主键生成策略 默认 雪花生成策略， 可以设置为自增
- 动态生成sql，这个原理是在启动的时候，mybatis plus会扫描所有mapper，然后自动注入sql
- 自动填充，比如时间
- 乐观锁

# Mybatis源码

## 一、使用方式

1. 基于statement key访问

   ```java
    Teacher teacher = sqlSession.selectOne("mybatis.learn.dao.mapper.TeacherMapper.getById",1);
   ```

   

2. 使用mapper动态代理访问，最终还是会根据statement key访问

   ```java
   TeacherMapper teacherMapper = sqlSession.getMapper(TeacherMapper.class);
   Teacher teacher = teacherMapper.getById(1);
   ```

### 动态代理原理

1.JDK代理，（接口代理）

```java
TeacherMapper teacherMapper = (TeacherMapper) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{TeacherMapper.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                System.out.println("proxy");
                return null;
            }
        });
```

这里生成了一个类如$Proxy0，继承java.lang.reflect.Proxy,实现TeacherMapper接口



原理：再内存创建了一个class字节码文件(.class)，通过类加载器，生成一个class对象，再通过反射机制生成一个实例对象,可以通过构造器生成。

method.getDeclaringClass() 获取该方法所属的class，即在哪个class里定义的

Proxy类有一个InvocationHandler属性

生成的类$Proxy0里的接口方法里，会调用ths.h.invoke()  h为创建是传进去的 InvocationHandler， Proxy类实例化代理类时，会将invocationHanler传进去。

## 二、架构原理

![image-20200704204438286](/Users/yudong/learn/alibaba/java/imgs/image-20200704204438286.png)

![image-20200704204939570](/Users/yudong/learn/alibaba/java/imgs/image-20200704204939570.png)

![image-20200704205020374](/Users/yudong/learn/alibaba/java/imgs/image-20200704205020374.png)

executor负责缓存的维护

![image-20200704205252581](/Users/yudong/learn/alibaba/java/imgs/image-20200704205252581.png)

解析过程：

![image-2020070533423397](/Users/yudong/learn/alibaba/java/imgs/image-20200705133423397.png)

有三个主要的解析器 Configure Mapper StatementXmLParser

### 一、创建SqlSessionFactory

SqlSessionFactory是包含Configuration, 基本mybatis是通过解析xml获取，根据Configuration创建SqlSession. 使用建造者模式，直白讲，就是使用多个简单的对象，一步步构建一个复杂的对象。mybatis的初始化工作非常复杂，不是一个constructure可以简单搞定的。



 ![未命名文件](/Users/yudong/learn/alibaba/java/imgs/未命名文件.png)

### 二、创建sqlSession

![image-20200705134340899](/Users/yudong/learn/alibaba/java/imgs/image-20200705134340899.png)

![未命名文件 (1)](/Users/yudong/learn/alibaba/java/imgs/未命名文件 (1).png)

Executor, 二级缓存CachingExecutor 使用装饰者模式,  比如SimpleExecutor套CachingExecutor

```java
public Executor newExecutor(Transaction transaction, ExecutorType executorType) {
    executorType = executorType == null ? defaultExecutorType : executorType;
    executorType = executorType == null ? ExecutorType.SIMPLE : executorType;
    Executor executor;
    if (ExecutorType.BATCH == executorType) {
      executor = new BatchExecutor(this, transaction);
    } else if (ExecutorType.REUSE == executorType) {
      executor = new ReuseExecutor(this, transaction);
    } else {
      executor = new SimpleExecutor(this, transaction);
    }
    if (cacheEnabled) {
      executor = new CachingExecutor(executor);
    }
    executor = (Executor) interceptorChain.pluginAll(executor);
    return executor;
  }
```

插件也在这里调用一次。(Executor) interceptorChain.pluginAll(executor)是一种无拦截的责任链+代理模式,对executor增强

```java
public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }
```

### 三、创建mapper

![image-20200705135028764](/Users/yudong/learn/alibaba/java/imgs/image-20200705135028764.png)

mapper的创建使用的是代理模式

```java
public class MapperProxyFactory<T> {

  private final Class<T> mapperInterface;
  private final Map<Method, MapperMethodInvoker> methodCache = new ConcurrentHashMap<>();

  public MapperProxyFactory(Class<T> mapperInterface) {
    this.mapperInterface = mapperInterface;
  }

  public Class<T> getMapperInterface() {
    return mapperInterface;
  }

  public Map<Method, MapperMethodInvoker> getMethodCache() {
    return methodCache;
  }

  @SuppressWarnings("unchecked")
  protected T newInstance(MapperProxy<T> mapperProxy) {
    return (T) Proxy.newProxyInstance(mapperInterface.getClassLoader(), new Class[] { mapperInterface }, mapperProxy);
  }

  public T newInstance(SqlSession sqlSession) {
    final MapperProxy<T> mapperProxy = new MapperProxy<>(sqlSession, mapperInterface, methodCache);
    return newInstance(mapperProxy);
  }

}
```

```java
configuration
public <T> T getMapper(Class<T> type, SqlSession sqlSession) {
    return mapperRegistry.getMapper(type, sqlSession);
  }
public <T> void addMapper(Class<T> type) {
    mapperRegistry.addMapper(type);
  }
```



一个mapper指的是接口，基本mybatis不能重复扫描。xml文件是定义mappedStatement. mapper接口如果要和xml对应，需要遵循一些约定，namespace要是接口限定名，节点id要和方法对应，包括parameteType, resultType都要一一对应。

扫描mapper的时候会扫描当前包下是否存在同名xml文件，如果存在加载并解析。

Mybatis-spring扫描mapper的几种方式

1. @Mapper

2. @MapperScan

3. ```java
   <bean id="userMapper" class="org.mybatis.spring.mapper.MapperFactoryBean">
     <property name="mapperInterface" value="org.mybatis.spring.sample.mapper.UserMapper" />
     <property name="sqlSessionFactory" ref="sqlSessionFactory" />
   </bean>
   ```

4. 在全局配置文件xml中定义<mapper class>使用class方式 或者package方式.



具体源码，是在MapperFactoryBean初始化之后，会讲mapper注入到configuration中。

之后注入service时，会调用MapperFactoryBean的getObject方法，即getSqlSession().getMapper(this.mapperInterface);获得mapper的代理对象。

创建mapper代理的过程，总结下来就是在mybatis初始化的时候，先解析配置，在mapperRegister里注册了mapper代理工厂(mapperProxyFactory)，用于生产代理；sqlSession.getMapper()创建的时候，先通过接口名获取mapperProxyFactory, mapperProxyFactory生产代理对象，代理对象的invokeHandler封装了sqlSession,接口类，methodCache.

一个sqlSession对象，封装了configuration和executor。

可以说mapper的所有操作都是由sqlSession完成，sqlSession根据接口方法名，查找对应statement，之后解析、执行、结果转换。

spring中具体是sqlSessionTemplate这个sqlSession实现，但它又把具体操作交给一个代理类sqlSessionProxy。

一个mapper代理实例，对应一个sqlSession.



### 四、查询流程

![image-20200705144349404](/Users/yudong/learn/alibaba/java/imgs/image-20200705144349404.png)

![image-20200705144449210](/Users/yudong/learn/alibaba/java/imgs/image-20200705144449210.png)

![image-20200705175844931](/Users/yudong/learn/alibaba/java/imgs/image-20200705175844931.png)

![image-20200705175913055](/Users/yudong/learn/alibaba/java/imgs/image-20200705175913055.png)

Executor将操作委派给statementHandler处理：

```java
public interface StatementHandler {

  Statement prepare(Connection connection, Integer transactionTimeout)
      throws SQLException;

  void parameterize(Statement statement)
      throws SQLException;

  void batch(Statement statement)
      throws SQLException;

  int update(Statement statement)
      throws SQLException;

  <E> List<E> query(Statement statement, ResultHandler resultHandler)
      throws SQLException;

  <E> Cursor<E> queryCursor(Statement statement)
      throws SQLException;

  BoundSql getBoundSql();

  ParameterHandler getParameterHandler();

}
```

statementHandler利用parameterHandler进行参数设置，利用resultSetHandler进行结果处理，这两个handler都利用了TypeHandler.

parameterHandler 从boundSql里获取parameterMappings, 通过typeHandler设置sql参数

resultSetHandler如下

![image-20200705181148091](/Users/yudong/learn/alibaba/java/imgs/image-20200705181148091.png)

### 插件原理

四大插件对象

Executor, StatementHandler,parameterHandler,resultSetHandler

```java
public interface Interceptor {

  Object intercept(Invocation invocation) throws Throwable;

  default Object plugin(Object target) {
    return Plugin.wrap(target, this);
  }

  default void setProperties(Properties properties) {
    // NOP
  }

}
```

```java
public class InterceptorChain {

  private final List<Interceptor> interceptors = new ArrayList<>();

  public Object pluginAll(Object target) {
    for (Interceptor interceptor : interceptors) {
      target = interceptor.plugin(target);
    }
    return target;
  }

  public void addInterceptor(Interceptor interceptor) {
    interceptors.add(interceptor);
  }

  public List<Interceptor> getInterceptors() {
    return Collections.unmodifiableList(interceptors);
  }

}
```

```java
Plugins.clas
public static Object wrap(Object target, Interceptor interceptor) {
  Map<Class<?>, Set<Method>> signatureMap = getSignatureMap(interceptor);
  Class<?> type = target.getClass();
  Class<?>[] interfaces = getAllInterfaces(type, signatureMap);
  if (interfaces.length > 0) {
    return Proxy.newProxyInstance(
        type.getClassLoader(),
        interfaces,
        new Plugin(target, interceptor, signatureMap));
  }
  return target;
}
```

只会为要拦截的接口创建代理，只拦截要拦截的方法



多个拦截，add(A) add(B) 则先执行B，后执行A， B对A包装，A对四大对象包装



一级缓存 存在BaseExecutor(executor父类）， SimpleExecutor, BatchExecutor,ReusedExecutor

BaseExecutor加了一个缓存逻辑，具体操作还是子类完成，这是模版方法。

二级缓存 （装饰器模式）

```java
if (cacheEnabled) {
  executor = new CachingExecutor(executor);
}
```

![image-20200705170352032](/Users/yudong/learn/alibaba/java/imgs/image-20200705170352032.png)

一级缓存 key与刷新

![image-20200705170544425](/Users/yudong/learn/alibaba/java/imgs/image-20200705170544425.png)

# 注意事项

1.定义实体类时候，成员变量使用包装类，不要使用基本类，比如int, boolean, 因为包装类的默认值是null，基本类型都有具体值，不便于mybatis处理，尤其是动态sql的时候