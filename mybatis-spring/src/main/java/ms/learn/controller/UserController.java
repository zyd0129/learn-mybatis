package ms.learn.controller;

import com.fasterxml.jackson.annotation.JsonView;
import ms.learn.models.User;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("users")
public class UserController {
    /**
     * /user/query?
     * /user/getInfo?id=1
     * /user/update?id=1
     * /user/delete?id=1
     * /user/create?name=1
     */


    /**
     * Pageable,page,size,sort
     *
     * @param name
     * @return
     */
    @GetMapping("query")
    @JsonView(User.UserSimpleView.class)
    public List<User> queryUsers(@RequestParam(name = "username", defaultValue = "tom") String name) {
        List<User> userList = new ArrayList<>();
        userList.add(new User());
        userList.add(new User());
        userList.add(new User());
        return userList;
    }

    @GetMapping("getInfo/{id:\\d+}")
    @JsonView(User.UserDetailView.class)
    public User getInfo(@PathVariable Integer id) {
        return new User();
    }

    @PostMapping("create")
    public User createUser(@Valid @RequestBody User user) {
        return user;
    }

}
