package mybatis.learn.dao.mapper;

import org.junit.Test;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class UserMapperProxyTest {

    @Test
    public void proxyTest() {

        TeacherMapper teacherMapper = (TeacherMapper) Proxy.newProxyInstance(this.getClass().getClassLoader(), new Class<?>[]{TeacherMapper.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                method.getName(); //获取方法名
                method.getDeclaringClass().getName(); //获取方法所属类名
                System.out.println(method.getDeclaringClass().getName() + "." + method.getName()); //这就可以获取statement key
                return null;
            }
        });
        teacherMapper.m();
    }
}
