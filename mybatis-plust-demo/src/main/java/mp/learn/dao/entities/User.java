package mp.learn.dao.entities;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class User {
    @TableId(type=IdType.AUTO) //id生成策略
    private int id;
    private String name;
    private String pwd;
    @TableField(fill = FieldFill.UPDATE) //自动填充
    private LocalDateTime gmtModified;

    @Version //乐观锁
    private int version;
    @TableLogic //逻辑删除，查询的时候自动加上筛选条件
    private int deleted;
}
