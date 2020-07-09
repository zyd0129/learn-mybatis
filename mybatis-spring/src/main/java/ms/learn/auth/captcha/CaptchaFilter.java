package ms.learn.auth.captcha;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

public class CaptchaFilter extends OncePerRequestFilter {

    //这里默认使用sendError->BasicErrorController统一处理，支持自定义处理，这就是可扩展性
    private CaptchaValidateFailureHandler captchaValidateSuccessHandler;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        //并不是所有url都需要验证码拦截，只需拦截要拦截的
        if (request.getRequestURI().equals("/login")) {

            HttpSession session = request.getSession(true);
            String captcha = (String) session.getAttribute("captcha");
            String iCaptcha = request.getParameter("captcha");
            try {
                validateCaptcha(iCaptcha, captcha);
            } catch (CaptchaException ex) {
                captchaValidateSuccessHandler.onValidateFailure(request, response, ex);
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private void validateCaptcha(String iCaptcha, String captcha) throws CaptchaException {
        if (StringUtils.isEmpty(iCaptcha)) {
            throw new CaptchaException("验证码不能为空");
        } else if (!iCaptcha.equals(captcha)) {
            throw new CaptchaException("验证码不正确");
        }
    }

    public void setCaptchaValidateSuccessHandler(CaptchaValidateFailureHandler captchaValidateSuccessHandler) {
        this.captchaValidateSuccessHandler = captchaValidateSuccessHandler;
    }
}
