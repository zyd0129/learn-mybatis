package ms.learn.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class SecurityController {

    @GetMapping("/hello")
    public String hello() {
        return "hello";
    }
    @GetMapping("/info")
    public String info() {
        return "info";
    }

}
