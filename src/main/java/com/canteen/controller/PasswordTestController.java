package com.canteen.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class PasswordTestController {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @PostMapping("/encode")
    public String encodePassword(@RequestParam String password) {
        String encodedPassword = passwordEncoder.encode(password);
        return "Original: " + password + "\nEncoded: " + encodedPassword;
    }

    @PostMapping("/verify")
    public String verifyPassword(@RequestParam String password, @RequestParam String encodedPassword) {
        boolean matches = passwordEncoder.matches(password, encodedPassword);
        return "Password: " + password + "\nEncoded: " + encodedPassword + "\nMatches: " + matches;
    }
}