package com.api.serverLaS.services;

import com.api.serverLaS.models.User;
import com.api.serverLaS.repositories.UserRepository;
import com.api.serverLaS.requests.AuthRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User create(AuthRequest request) {
        return userRepository.create(request.getFio(), request.getGroup());
    }
}
