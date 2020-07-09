package ms.learn.auth.captcha;


import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CaptchaValidateFailureHandler {
    void onValidateFailure(HttpServletRequest request,
                           HttpServletResponse response, CaptchaException exception)
            throws IOException, ServletException;
}
