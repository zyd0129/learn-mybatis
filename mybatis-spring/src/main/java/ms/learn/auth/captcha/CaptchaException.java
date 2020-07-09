package ms.learn.auth.captcha;

import javax.naming.AuthenticationException;

public class CaptchaException extends AuthenticationException {
    public CaptchaException(String explanation) {
        super(explanation);
    }
}
