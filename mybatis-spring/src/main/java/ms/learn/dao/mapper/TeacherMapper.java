package ms.learn.dao.mapper;


import ms.learn.dao.entities.Teacher;
import org.apache.ibatis.annotations.Select;

public interface TeacherMapper {
//    @Select("select * from teacher where id=#{id}")
    Teacher getById(int id);
}
