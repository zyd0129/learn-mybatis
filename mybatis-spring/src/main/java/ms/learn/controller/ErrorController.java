package ms.learn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.UUID;

//@RestController
public class ErrorController {

    //    @GetMapping("/error")
    public void error(HttpServletRequest request, HttpServletResponse response) throws IOException {

        System.out.println("error:" + request.getAttribute("javax.servlet.error.status_code")); //300
        response.sendError(300);
    }

    @GetMapping("/sendError")
    public void sendError(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setAttribute("error", "sendError");
        response.sendError(300);
    }

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }

    @GetMapping("/captcha")
    public String getCaptcha(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(true);
        String captcha = UUID.randomUUID().toString().substring(7, 10);
        session.setAttribute("captcha", captcha);
        return captcha;
    }
}
