package ms.learn;

import ms.learn.dao.entities.Teacher;
import ms.learn.dao.mapper.TeacherMapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("ms.learn.dao.mapper")
public class Application implements CommandLineRunner {

    @Autowired
    TeacherMapper teacherMapper;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        Teacher teacher = teacherMapper.getById(1);
        System.out.println(teacher);
    }
}
