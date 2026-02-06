package com.canteen.dto;

import com.canteen.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
}
