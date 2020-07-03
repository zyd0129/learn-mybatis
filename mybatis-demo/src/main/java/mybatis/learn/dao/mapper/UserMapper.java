package mybatis.learn.dao.mapper;

import mybatis.learn.dao.entities.User;

import java.util.List;

public interface UserMapper {
    List<User> getUserList();
    List<User> getUserLike(String name);
}
