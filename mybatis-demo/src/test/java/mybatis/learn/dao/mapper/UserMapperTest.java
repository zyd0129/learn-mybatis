package mybatis.learn.dao.mapper;

import mybatis.learn.dao.entities.Teacher;
import mybatis.learn.dao.entities.User;
import mybatis.learn.util.MybatisUtils;
import org.apache.ibatis.session.SqlSession;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class UserMapperTest {

    @Test
    public void statementId() {
        SqlSession sqlSession = MybatisUtils.getSqlSession();

        Teacher teacher = sqlSession.selectOne("mybatis.learn.dao.mapper.TeacherMapper.getById",1);
        System.out.println(teacher);
        sqlSession.close();
    }

    @Test
    public void getUserListTest(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();

        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = userMapper.getUserList();
        users.forEach(o -> System.out.println(o.toString()));
        sqlSession.close();
    }
    @Test
    public void getUserLikeTest(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();

        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        List<User> users = userMapper.getUserLike("2");
        users.forEach(o -> System.out.println(o.toString()));
        sqlSession.close();
    }

    @Test
    public void getTeacherByIdTest(){
        SqlSession sqlSession = MybatisUtils.getSqlSession();

        TeacherMapper teacherMapper = sqlSession.getMapper(TeacherMapper.class);

        Teacher teacher = teacherMapper.getById(1);
        System.out.println(teacher);
        sqlSession.close();
    }

}