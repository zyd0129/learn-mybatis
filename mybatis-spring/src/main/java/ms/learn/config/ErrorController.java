package ms.learn.config;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

//@RestController
public class ErrorController {

    @GetMapping("/error")
    public String error() {
        return "error";
    }
}
