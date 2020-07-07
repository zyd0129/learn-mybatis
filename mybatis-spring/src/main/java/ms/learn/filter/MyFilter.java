package ms.learn.filter;

import lombok.extern.slf4j.Slf4j;
import ms.learn.dao.mapper.TeacherMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import java.io.IOException;

@Component
@Slf4j
@Order(-110)
public class MyFilter implements Filter {

//    @Autowired
//    TeacherMapper teacherMapper;

    public MyFilter() {
    }

    private String name;

    public MyFilter(String name) {
        this.name=name;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        System.out.println("enter myFilter " + name);
        log.info("ct:"+Thread.currentThread().getId());
        chain.doFilter(request, response);
        System.out.println("leave myFilter " + name);
    }

//    @Override
//    public int getOrder() {
//        return -110;
//    }
}
