package mybatis.learn.dao.mapper;

import mybatis.learn.dao.entities.Teacher;
import mybatis.learn.dao.entities.User;

import java.util.List;

public interface TeacherMapper  extends  BaseMapper{
    Teacher getById(int id);
}
