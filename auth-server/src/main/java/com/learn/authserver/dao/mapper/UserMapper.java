package com.learn.authserver.dao.mapper;



import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.userdetails.User;

import java.util.List;

public interface UserMapper {
    @Select("select * from user where id=#{id}")
    User getById(Integer id);
}
