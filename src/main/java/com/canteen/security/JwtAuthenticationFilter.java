package com.canteen.security;

import com.canteen.entity.User;
import com.canteen.service.UserService;
import com.canteen.utils.JwtUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT认证过滤器
 */
public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserService userService;

    /**
     * 构造函数
     * @param authenticationManager AuthenticationManager
     * @param jwtUtils JwtUtils
     * @param userService UserService
     */
    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserService userService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userService = userService;
        setFilterProcessesUrl("/auth/login");
    }

    /**
     * 尝试认证
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @return Authentication
     * @throws AuthenticationException 认证异常
     */
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        if (!"POST".equals(request.getMethod()) || !"/api/auth/login".equals(request.getRequestURI())) {
            throw new AuthenticationException("Invalid authentication request") {};
        }

        try {
            ObjectMapper mapper = new ObjectMapper();
            User loginUser = mapper.readValue(request.getInputStream(), User.class);
            UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                    loginUser.getUsername(), loginUser.getPassword());
            return authenticationManager.authenticate(token);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 认证成功
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param chain FilterChain
     * @param authResult Authentication
     * @throws IOException 异常
     * @throws ServletException 异常
     */
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        org.springframework.security.core.userdetails.User user = (org.springframework.security.core.userdetails.User) authResult.getPrincipal();
        String token = jwtUtils.generateToken(user.getUsername());

        // 获取用户信息
        User userInfo = userService.findByUsername(user.getUsername());

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "登录成功");
        result.put("data", Map.of(
                "token", token,
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
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }

    /**
     * 认证失败
     * @param request HttpServletRequest
     * @param response HttpServletResponse
     * @param failed AuthenticationException
     * @throws IOException 异常
     * @throws ServletException 异常
     */
    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        if (!"POST".equals(request.getMethod()) || !"/api/auth/login".equals(request.getRequestURI())) {
            response.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("message", "登录失败，用户名或密码错误");

        response.setContentType("application/json; charset=utf-8");
        response.getWriter().write(new ObjectMapper().writeValueAsString(result));
    }
}
