package ms.learn.auth.captcha;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class DefaultCaptchaValidateFailureHandler implements CaptchaValidateFailureHandler {
    @Override
    public void onValidateFailure(HttpServletRequest request, HttpServletResponse response, CaptchaException exception) throws IOException, ServletException {
        response.sendError(400, "验证码错误");
    }
}
