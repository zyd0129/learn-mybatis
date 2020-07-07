package ms.learn.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
public class SecurityController {

    @GetMapping("/info")
    public String info() {
        return "info";
    }

    @GetMapping("/method")
    @PreAuthorize("hasRole('ADMIN')")
    public String method() {
        return "method";
    }

}
