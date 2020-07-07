package ms.learn.exception;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;


@RestControllerAdvice
public class GlobalAdvice {

    @ExceptionHandler(AccessDeniedException.class)
    public void handlerAccessDeniedException(AccessDeniedException ex) {
        System.out.println(ex);
    }
}
