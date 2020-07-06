package mybatis.learn.dao.mapper;

import mybatis.learn.dao.entities.StatusEnum;
import org.junit.Test;

public class EnumTest {
    @Test
    public void test() {
        System.out.println(StatusEnum.SUCCESS.ordinal());
        System.out.println(StatusEnum.SUCCESS.name());
    }
}
