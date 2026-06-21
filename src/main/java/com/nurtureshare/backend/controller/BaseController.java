package com.nurtureshare.backend.controller;

import com.nurtureshare.backend.exception.ResourceNotFoundException;
import com.nurtureshare.backend.model.User;
import com.nurtureshare.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class BaseController {

    @Autowired
    protected UserRepository userRepository;

    protected User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
