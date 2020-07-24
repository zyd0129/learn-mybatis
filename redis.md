redis每秒11万读，8万写 3高 高并发 高可拓 高性能

基于内存的可持久化的k-v数据库

单线程》 线程上下文切换  基于内存单线程比较快，工作线程是单线程，底层使用 epoll,或者select,多路复用器   io是多线程的，读取是单线程的，

### 持久化：rdb aof, 

rdb快照，fork一个进程备份，恢复快，会丢失一些数据,默认 900 1  300 10. 60 10000；

aof追加方式，最多损失2s数据，恢复慢；一般使用rdb;

no-sql: key-value, 文档型mongodb, 图关系Neo4j，列存储hbase;    es

redis默认16个库，集群下只有db0

常用命令：

keys  exists  ttl expire type

String类型： 可以用来存储图片，二进制安全，只保存字节码，客户端传什么，就存什么。

mset是原子操作，批量设置值

list 双向链表，可以用来实现队列、栈、阻塞队列

lpush  lrange

lpop  key  20   如果取不到 阻塞

lpopRpush

应用场景：关注列别，粉丝列表，热点新闻，任务队列

set  使用hash表实现 交集、差集、并集  共同好友 

zset 有序   zadd key  [score] [member]  zrevRange  zrange  适合，元素少且较小时，使用ziplist，之后使用skipList 最高64层。跳表怎么插入删除？？

应用场景：排行榜、带权重的队列（优先级队列）

其他类型： 

geo 

 hyperloglog(不重复统计，基于概率统计基数，但不存储数据，等价于 统计set的元素个数) 用于日活、月活，海量数据下，也仅需12k

bitmap   比如记录打卡天数   setbit  getbit  bitcount， 属于字符串类型；1年内任意时间窗用户登录天数 权限

### 主从复制

**Info** **replication**

默认情况下，都是主节点;一般情况下只配置主机

 **slaveof  127.0.0.1 6379**

主机写，从机不能写，写的话会报错；主机挂掉了，如果没有设置哨兵模式，从机还是从机，依旧连接到主机；

第一次连接到主机的时候 全量复制；后续是增量复制

主从两种模式：1）主从 2）从也当主，M->SM->S, 链路模式 SM仍然无法写入

slaveof no one。当主机断了之后，从机手动输入命令，变为主机

### 哨兵模式

（小公司使用,故障转移）

由手动切换，变为自动切换。由哨兵监控，当mater挂了之后，哨兵之间会选举出新的master。切换默认需要30s，这期间存在漏洞，这30s内服务器压力上升。 哨兵模式主要是主从切换，防止单点故障。

Redis Sentinel 集群看成是一个 ZooKeeper 集群，一般是由 3～5 个节点组成，
负责持续监控主从节点的健康，当主节点挂掉时，自动选择一个最优的从节点切换为主节点。

- 客户端来连接集群时，会首先连接 sentinel，通过 sentinel 来查询主节点的地址，然后再去连接主节点进行数据交互。
- 当主节点发生故障时，客户端会重新向 sentinel 要地址，sentinel 会将最新的主节点地址告诉客户端。如此应用程序将无需重启即可自动完成节点切换。

### Redis集群

（大公司使用） 至少3台主节点，至少3台从节点

由多个主从节点群组成的分布式服务，没有中心节点，可水平扩展，可以再先扩展1000节点，那么redis集群可支持的并发就是 10, 0000 * 1000 。牛逼

![image-20200724103458953](D:\learn\learn-mybatis\img\image-20200724103458953.png)

集群搭建 修改redis配置文件： cluster-enabled yes

还需使用ruby， redus-trib.rb  create --

```
ruby redis-trib.rb create --replicas 1 10.180.157.199:6379 10.180.157.200:6379 10.180.157.201:6379 10.180.157.202:6379  10.180.157.205:6379  10.180.157.208:6379
```

这一条命令，就搭建完集群了，主从比 1：1，前面是主，后面是从。发生故障，主从会自动切换。

给主机分配槽位，默认平均分配

集群验证：  redis-cli -c -u -a    -c表示以集群模式连接

怎么做重定向？怎么分片？怎么主从切换？水平扩展？？

Jedis没有重定向功能，redis-cli有重定向功能。每台机子，都有重定向功能；会对key做分片，如果是其它组的，路由到其它节点。

crc16对key进行hash，然后对16384取余；

cluster node 查看集群

扩容步骤：

1.加入集群

2.分配槽位，指定分配多少槽位，选择给哪个节点分配槽位，从哪里分（可以选择所有节点，也可指定某些节点），迁移数据 

jedis连接 redis-cluster原理

![image-20200724131801324](D:\learn\learn-mybatis\img\image-20200724131801324.png)

内置集群问题：

1.无中心节点的集群架构，依靠gossop协议（谣言传播）协同自动化修复集群的状态。内部节点需要不断进行ping/pong，造成大量请求。而且有消息延时和消息冗余的问题。

A告诉C,B down了; D告诉C B down了；C认为B down了，  C告诉A down了。

2.数据迁移是一个同步操作

3.出现的比较晚，很多公司都有自己的集群方案

### 一致性哈希

一致性哈希，哈希环，虚拟节点（解决倾斜问题，很多数据落在一个节点）；找到第一个大于的节点；只需要两个节点进行数据迁移，如果环上有10000个节点，那么数据迁移的比例就相对很少。连续区间。

redis-cluster使用哈希槽的概念，解决数据倾斜的问题，非连续区间，哈希槽覆盖整个哈希环。

### 代理模式 实现集群

有一个代理节点，类似mycat;Twemproxy Codis



https://www.jianshu.com/p/84dbb25cc8dc深入剖析Redis - Redis集群模式搭建与原理详解

### 缓存宕机之后 如何避免大量请求？

白名单

缓存宕机之前的一段时间里，会将请求的数据在系统中的有无，记录在一个Map中。当缓存宕机后，首先在Map中判断是否含有数据，有则回源DB，没有的话就直接返回结果。

bloomFilter

布隆过滤器的原理是，当一个元素被加入集合时，通过K个Hash函数将这个元素映射成一个位数组中的K个点，把它们置为1。检索时，我们只要看看这些点是不是都是1就（大约）知道集合中有没有它了：如果这些点有任何一个0，则被检元素一定不在；如果都是1，则被检元素很可能在。这就是布隆过滤器的基本思想

布隆过滤器的错误率，判定存在，不一定存在，这是由于哈希碰撞决定的，在哈希分布均匀的情况下，错误率为1/k，k为位数组大小；可以通过多次哈希降低错误率，n次哈希，错误率就是 1/（k^n);实现可以借助redis的bitmap实现；那什么时候标记，可以同步标记或异步标记。

缓存穿透，数据库中坑定不存在的，在redis中查不到，去查mysql。设置key-null

缓存击穿，只有一条热点数据，缓存过了有效期，但实际还是热点，这样在0.1s内，仍然有大量请求，数据库会有很大压力

​	解决方案：使用分布式乐观锁，jdk锁只能是单机，多机需要分布式锁。抢不到锁就失败，再次请求就会走缓存。

缓存雪崩，有效期集中失效，比如采用redis集群，采用取模，扩展的时候会造成缓存失效。

​				1.集中失效 解决方案：如果设置了一致的有效期，可以通过设置随机有效期

​				 2.redis挂了 搞redis集群，集群分为切片集群、主从集群；针对切片集群，为了避免扩容时数据复制，采用一致型哈希

​	



分布式锁，1.如果获取锁的机器挂了，锁永远不会释放，这种情况需要设置锁的过期时间 ;这会导致重复释放锁的问题，可能释放了别人的锁。这里关键怎么设置锁的有效期？有效期过长，会少卖票；过短，会造成“多”卖票；可以通过zookipper来实现分布式锁解决。



### redis事务

执行多条命令  multi... exec  错误的命令不影响其他命令; 执行中不会被其它命令插入，不许加塞。不支持回滚  事务是由一组命令组成，也具有原子性。

悲观锁，任何时候都加锁；乐观锁，认为任何时候哦都不需要锁，只再修改的时候判断是否被修改了。 

discard 放弃执行

### redis乐观锁

watch key  multi...exec  如果事务开始执行之前，检测到监控对象发生了变化，放弃执行。



springboot集成 redis， lettuce

自定义redisTemplate:

默认是Jdk序列化，会对字符串进行转义，添加一些字符。如果直接存储一个对象，需要先序列化。

```
spring.redis.host=127.0.0.1
spring.redis.port=6379
#连接池最大链接数默认值为8
spring.redis.lettuce.pool.max-active=8
#连接池最大阻塞时间（使用负值表示没有限制）默认为-1
spring.redis.lettuce.pool.max-wait=-1
#连接池中的最大空闲连接数 默认为8
spring.redis.lettuce.pool.max-idle=8
#连接池中的最小空闲连接数 默认为8
spring.redis.lettuce.pool.min-idle=0
```

```
@Bean
    RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
 
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer(Object.class);
        // 设置值（value）的序列化采用Jackson2JsonRedisSerializer。
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        // 设置键（key）的序列化采用StringRedisSerializer。
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
 
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }
```

```
RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        Jackson2JsonRedisSerializer<Object> redisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        redisSerializer.setObjectMapper(mapper);
        template.setValueSerializer(redisSerializer);

        template.setKeySerializer(new StringRedisSerializer());
        template.afterPropertiesSet();

```



redis整合 哨兵模式、集群模式

```yml
  redis:
##单机
#    host: 172.21.0.211
#    port: 6379
##
##集群
    cluster:
      nodes:
      - 172.21.0.211:7000
      - 172.21.0.211:7001
      - 172.21.0.211:7002
      - 172.21.0.211:7003
      - 172.21.0.211:7004
##哨兵
    sentinel:
      master: mymaster
      nodes:
      - 172.21.0.211:7005
      - 172.21.0.211:7006
      - 172.21.0.211:7007
##
    jedis:
      pool:
### 连接池最大连接数（使用负值表示没有限制） 
        max-active: 9
### 连接池最大阻塞等待时间（使用负值表示没有限制）
        max-wait: -1
### 连接池中的最大空闲连接 
        max-idle: 9
### 连接池中的最小空闲连接 
        min-idle: 0
### Redis数据库索引(默认为0) 
    database: 0
### 连接超时时间（毫秒） 
    timeout: 60000

```

### 分布式锁

错误方法：
setnx获取锁，拿到锁用expire给锁加一个过期时间，防止锁忘记释放。如果setnx执行之后expire执行之前，线程死掉，那锁就永远得不到释放，发生死锁。
Long result = jedis.setnx(lockKey, requestId);
if (result == 1) {
// 线程死掉，无法设置过期时间，发生死锁
jedis.expire(lockKey, expireTime);
}

最佳实践：set=setnx和expire，一条指令，把setnx和expire原子化结合起来。

set key value [ex seconds] [px milliseconds] [nx|xx]
ex seconds： 为键设置秒级过期时间。
px milliseconds： 为键设置毫秒级过期时间。
nx： 键必须不存在， 才可以设置成功， 用于添加。
xx： 与nx相反， 键必须存在， 才可以设置成功， 用于更新。

负责均衡 LVS/nginx

io threads



netty---->io

tomcat/nginx/lbs -> 负载均衡

gateway

zookipper