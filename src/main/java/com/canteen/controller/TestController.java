package com.canteen.controller;

import com.canteen.entity.User;
import com.canteen.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class TestController {

    @Autowired
    private UserService userService;

    @GetMapping("/users")
    public List<User> getUsers() {
        return userService.list();
    }

    @GetMapping("/user/{username}")
    public User getUserByUsername(String username) {
        return userService.findByUsername(username);
    }
}
