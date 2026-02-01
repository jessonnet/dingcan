package com.canteen.security;

import com.canteen.entity.User;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class CustomAuthenticationFilter extends OncePerRequestFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;
    private final ObjectMapper objectMapper;

    public CustomAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!"/api/auth/login".equals(request.getRequestURI()) || !"POST".equals(request.getMethod())) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            User loginUser = objectMapper.readValue(request.getInputStream(), User.class);
            System.out.println("Login attempt for user: " + loginUser.getUsername());
            System.out.println("Password received: " + loginUser.getPassword());
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    loginUser.getUsername(), loginUser.getPassword());
            System.out.println("Authentication token created");
            Authentication authResult = authenticationManager.authenticate(token);
            System.out.println("Authentication successful");

            org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
            String jwtToken = jwtUtils.generateToken(user.getUsername());

            User userInfo = userService.findByUsername(user.getUsername());

            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "登录成功");
            result.put("data", Map.of(
                    "token", jwtToken,
                    "user", Map.of(
                            "id", userInfo.getId(),
                            "username", userInfo.getUsername(),
                            "name", userInfo.getName(),
                            "role", userService.getRoleNameByUserId(userInfo.getId()),
                            "department", userInfo.getDepartmentName(),
                            "phone", userInfo.getPhone(),
                            "email", userInfo.getEmail()
                    )
            ));

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (AuthenticationException e) {
            System.out.println("Authentication failed: " + e.getMessage());
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "登录失败，用户名或密码错误: " + e.getMessage());

            response.setContentType("application/json; charset=utf-8");
            response.getWriter().write(objectMapper.writeValueAsString(result));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
