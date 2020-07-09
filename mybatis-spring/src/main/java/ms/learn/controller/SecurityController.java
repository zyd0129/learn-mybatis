package ms.learn.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api")
@Api(tags = "安全方法")
public class SecurityController {

    @GetMapping("/info")
    public String info() {
        return "info";
    }

    @GetMapping("/method")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("权限方法")
    public String method(@ApiParam("用户名") String username, String password) {
        return "method";
    }

}
