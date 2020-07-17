## 事务与锁

 show variables like 'autocommit'

以库存举例

事务A

 update xxx set amount = ammount -1



事务的隔离性，不能解决并发问题

隔离性：相当于我在独自操作数据库



update 会自动加锁，另一个事务B会等待A提交事务，提交事务的时候，释放锁。

```mysql
mysql> select * from product;
+----+------+--------+
| id | name | amount |
+----+------+--------+
|  1 | p1   |      5 |
+----+------+--------+
1 row in set (0.00 sec)

mysql> update  product set amount=amount-1 where id=1;
Query OK, 1 row affected (0.00 sec)
Rows matched: 1  Changed: 1  Warnings: 0

mysql> select * from product;
+----+------+--------+
| id | name | amount |
+----+------+--------+
|  1 | p1   |     -1 |
+----+------+--------+
1 row in set (0.00 sec)
```

读的时候，读的是是读的版本链中 transaction_id 小于= 当前tansaction的transaction_id,且 不在readView的当前活跃事务列表中的 版本

即 transcation_id <= cur_transaction_id && transaction_id not in  readView.m_ids[]

Multi-Version Concurrency Control 多版本并发控制，保证**读的并发性**,保证读的是自己的版本，但是写的时候，是修改版本链当中最新的版本。

事务的隔离性并不能做到完全隔离，一般指读的隔离性

readView+行级锁，版本链中至多存在一个未提交事务的版本

ReadView 属性：

m_ids

min_trx_id

max_trx_id

creator_id

可重复读与读已提交的 实现区别：

我的方案：

读已提交：版本链 transaction_id  not in  readView.m_ids[]

可重复读： transcation_id <= cur_transaction_id && transaction_id not in  readView.m_ids[]

周瑜讲的：

区别在于readView的生成时机，read_committed 是每次select查询都生成一个readReview；而read_repeatable只是在第一次查询的时候生成一个readReview;后续查询复用这个readView, 这个不太理解



select * from XX 这个不会请求锁，所以无论你加了读锁（共享锁)、还是写锁（排他锁)，都不会阻塞。

读写锁

读-写  写-读 写-写 阻塞

读-读  不阻塞



select *   lock in share mode    读锁

select *   for  update  写锁



insert\updat\delete 默认加锁，

隐式锁是什么？



read_committed下的锁效果，只对查出的行加锁

主键/唯一   select * from where id=1   for update  行锁

普通索引     只对查出的行加锁

普通字段 

read_repeatable下的锁效果

插入 间隙锁  解决幻读？？？

update * for where a>5; 不仅对 a>5的加锁，还有间隙锁，其他事务所有a>5都不能插入 



不走索引，所有行都锁住，间隙



写意向锁  读意向锁

join算法原理

https://zhuanlan.zhihu.com/p/54275505

本质是嵌套循环，有两种优化思路：

1. 通过内层索引减少内层循环次数 内存循环有索引 Index Nested-Loop Join
2. 通过缓存外层数据，减少外层循环，内存循环无索引 Block Nested-Loop Join 这种情况不推荐使用join

笛卡尔积，不是等值连法；Join是等值连法。

join 默认是inner join

**在决定哪个表做驱动表的时候，应该是两个表按照各自的条件过滤，过滤完成之后，计算参与join的各个字段的总数据量，数据量小的那个表，就是“小表”，应该作为驱动表。**



数据库连接怎么设置？长连接，不会释放内存，会发生OOM??

Redo log/binlog

Redo log   Write-Ahead Logging，它的关键点就是先写日志，再写磁盘

具体来说，当有一条记录需要更新的时候，InnoDB引擎就会先把记录写到redo log（粉板）里面，并更新内存，这个时候更新就算完成了。同时，InnoDB引擎会在适当的时候，将这个操作记录更新到磁盘里面，而这个更新往往是在系统比较空闲的时候做，这就像打烊以后掌柜做的事。

InnoDB的redo log是固定大小的，比如可以配置为一组4个文件，每个文件的大小是1GB，那么这块“粉板”总共就可以记录4GB的操作。从头开始写，写到末尾就又回到开头循环写

binlog

这两种日志有以下三点不同。

1. redo log是InnoDB引擎特有的；binlog是MySQL的Server层实现的，所有引擎都可以使用。
2. redo log是物理日志，记录的是“在某个数据页上做了什么修改”；binlog是逻辑日志，记录的是这个语句的原始逻辑，比如“给ID=2这一行的c字段加1 ”。
3. redo log是循环写的，空间固定会用完；binlog是可以追加写入的。“追加写”是指binlog文件写到一定大小后会切换到下一个，并不会覆盖以前的日志。

连接段提交

1. 写redolog  状态设置成prepared 2.写Binglog 3.讲redolog 状态设置成committed

保证一致性（还是不能一定保证)

binlog是用来做数据恢复的，增量恢复，在某个全量备份基础上。

回滚日志删除

回滚日志总不能一直保留吧，什么时候删除呢？答案是，在不需要的时候才删除。也就是说，系统会判断，当没有事务再需要用到这些回滚日志时，回滚日志会被删除。

什么时候才不需要了呢？就是当系统里没有比这个回滚日志更早的read-view的时候。

基于上面的说明，我们来讨论一下为什么建议你尽量不要使用长事务。