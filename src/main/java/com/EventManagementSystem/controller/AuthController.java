package com.EventManagementSystem.controller;


import com.EventManagementSystem.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.EventManagementSystem.dto.LoginRequest;
import com.EventManagementSystem.dto.RegisterRequest;
import com.EventManagementSystem.model.User;
import com.EventManagementSystem.service.UserService;
import com.EventManagementSystem.util.ValidationUtil;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final ValidationUtil validationUtil;

    @Autowired
    public AuthController(UserService userService, ValidationUtil validationUtil) {
        this.userService = userService;
        this.validationUtil = validationUtil;
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@RequestBody LoginRequest request) {
        validationUtil.validateEmail(request.getEmail());
        validationUtil.validatePassword(request.getPassword());

        User user = userService.login(request.getEmail(), request.getPassword());
        UserResponse userResponse = userService.convertToUserResponse(user);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        validationUtil.validateName(request.getName());
        validationUtil.validateEmail(request.getEmail());
        validationUtil.validatePhone(request.getPhone());
        validationUtil.validatePassword(request.getPassword());

        User user = userService.register(request.getName(), request.getEmail(), request.getPhone(),
                request.getPassword(), request.getRole());
        UserResponse response = userService.convertToUserResponse(user);
        return ResponseEntity.ok(response);
    }
}
