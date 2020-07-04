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



# Mybatis Plus

- **强大的 CRUD 操作**：内置通用 Mapper、通用 Service，仅仅通过少量配置即可实现单表大部分 CRUD 操作，更有强大的条件构造器，满足各类使用需求. 直接继承BaseMap即可
- 主键生成策略 默认 雪花生成策略， 可以设置为自增
- 动态生成sql
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

## 二、动态代理原理

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