package ms.learn.exception;

import lombok.extern.slf4j.Slf4j;
import ms.learn.common.ApiResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Map;

@RestController
@Slf4j
public class ExceptionController implements ErrorController {

    @Autowired
    private ErrorAttributes errorAttributes;

    /**
     * 默认错误
     */
    private static final String path_default = "/error";

    @Override
    public String getErrorPath() {
        return path_default;
    }

    /**
     * JSON格式错误信息
     */
    @RequestMapping(value = path_default,  produces = {MediaType.APPLICATION_JSON_VALUE})
    public  Map<String, Object> error(HttpServletRequest request) {
        log.info("ct:"+Thread.currentThread().getId());
        WebRequest webRequest = new ServletWebRequest(request);
        Map<String, Object> body = this.errorAttributes.getErrorAttributes(webRequest, true);
//        Arrays.asList(webRequest.getAttributeNames(RequestAttributes.SCOPE_REQUEST)).forEach(System.out::println);
        return body;
    }

}