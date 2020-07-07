package ms.learn.config;

import ms.learn.filter.MyFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * filter 测试
     * @return
     */

//    @Bean
    public FilterRegistrationBean registrationBean(){
        FilterRegistrationBean<MyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MyFilter("zyd"));
        registration.setUrlPatterns(Collections.singletonList("/*"));
        return registration;
    }

//    @Bean
    public MyFilter myFilter3() {
        return new MyFilter("world");
    }
//    @Bean
    public MyFilter myFilter2() {
        return new MyFilter("hello");
    }
}
