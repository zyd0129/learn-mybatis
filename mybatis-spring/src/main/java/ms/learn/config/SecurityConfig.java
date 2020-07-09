package ms.learn.config;

import ms.learn.auth.captcha.CaptchaFilter;
import ms.learn.auth.captcha.DefaultCaptchaValidateFailureHandler;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig extends WebSecurityConfigurerAdapter implements ApplicationContextAware {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
//        http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
        http.formLogin()
                .and().httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/api/admin").hasRole("ADMIN")
                .antMatchers("/api/info").hasRole("USER")
                .antMatchers("/api/**").authenticated()
//                .antMatchers("/error").permitAll()
                .anyRequest().permitAll();


        CaptchaFilter captchaFilter = new CaptchaFilter();
        captchaFilter.setCaptchaValidateSuccessHandler(new DefaultCaptchaValidateFailureHandler());

        http.addFilterBefore(captchaFilter, BasicAuthenticationFilter.class);

    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.inMemoryAuthentication()
                .withUser("admin").password("{noop}123456").roles("ADMIN")
                .and()
                .withUser("user").password("{noop}123456").roles("USER");
    }
}
