redis每秒11万读，8万写 3高 高并发 高可拓 高性能

基于内存的可持久化的k-v数据库

单线程》 线程上下文切换  基于内存单线程比较快

### 持久化：rdb aof, 

rdb快照，fork一个进程备份，恢复快，会丢失一些数据,默认 900 1  300 10. 60 10000；

aof追加方式，最多损失2s数据，恢复慢；一般使用rdb;

no-sql: key-value, 文档型mongodb, 图关系Neo4j，列存储hbase;    es

redis默认16个库，集群下只有db0

常用命令：

keys  exists  ttl expire type

String类型： 可以用来存储图片，二进制安全

mset是原子操作，批量设置值

list 链表，可以用来实现队列、栈、阻塞队列

lpush  lrange

lpop  key  20   如果取不到 阻塞

lpopRpush

应用场景：关注列别，粉丝列表，热点新闻，任务队列

set  使用hash表实现

zset 有序   zadd key  [score] [member]

应用场景：排行榜、带权重的队列（优先级队列）

其他类型： 

geo 

 hyperloglog(不重复统计，基于概率统计基数，但不存储数据，等价于 统计set的元素个数) 用于日活、月活，海量数据下，也仅需12k

bitmap   比如记录打卡天数   setbit  getbit  bitcount

### 主从复制

**Info** **replication**

默认情况下，都是主节点;一般情况下只配置主机

 **slaveof  127.0.0.1 6379**

主机写，从机不能写，写的话会报错；主机挂掉了，如果没有设置哨兵模式，从机还是从机，依旧连接到主机；

第一次连接到主机的时候 全量复制；后续是增量复制

主从两种模式：1）主从 2）从也当主，M->SM->S, 链路模式 SM仍然无法写入

slaveof no one。当主机断了之后，从机手动输入命令，变为主机

### 缓存宕机之后 如何避免大量请求？

白名单

缓存宕机之前的一段时间里，会将请求的数据在系统中的有无，记录在一个Map中。当缓存宕机后，首先在Map中判断是否含有数据，有则回源DB，没有的话就直接返回结果。

bloomFilter

布隆过滤器的原理是，当一个元素被加入集合时，通过K个Hash函数将这个元素映射成一个位数组中的K个点，把它们置为1。检索时，我们只要看看这些点是不是都是1就（大约）知道集合中有没有它了：如果这些点有任何一个0，则被检元素一定不在；如果都是1，则被检元素很可能在。这就是布隆过滤器的基本思想

布隆过滤器的错误率，判定存在，不一定存在，这是由于哈希碰撞决定的，在哈希分布均匀的情况下，错误率为1/k，k为位数组大小；可以通过多次哈希降低错误率，n次哈希，错误率就是 1/（k^n);实现可以借助redis的bitmap实现；那什么时候标记，可以同步标记或异步标记。

缓存穿透，在redis中查不到，去查mysql。

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

