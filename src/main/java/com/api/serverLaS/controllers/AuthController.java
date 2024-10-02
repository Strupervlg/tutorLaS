package com.api.serverLaS.controllers;

import com.api.serverLaS.models.User;
import com.api.serverLaS.requests.AuthRequest;
import com.api.serverLaS.response.AuthResponse;
import com.api.serverLaS.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    public UserService userService;

    @PostMapping("/auth")
    public AuthResponse auth(@RequestBody AuthRequest authRequest) {
        User user = userService.create(authRequest);
        return new AuthResponse(user.getUid());
    }
}
