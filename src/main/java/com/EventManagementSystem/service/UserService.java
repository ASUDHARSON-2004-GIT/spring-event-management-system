package com.EventManagementSystem.service;

import java.util.List;

import com.EventManagementSystem.dto.UserResponse;
import com.EventManagementSystem.model.Role;
import com.EventManagementSystem.model.User;

public interface UserService {

    User register(String name, String email, String phone, String password, Role role);

    User login(String email, String password);

    User getProfile(long userId);

    void updateProfile(long userId, String name, String phone);

    List<User> listUsersByRole(Role role);

    List<User> listAllUsers();

    void deactivateUser(long userId);

    void activateUser(long userId);

    long countByRole(Role role);

    void deleteUser(long userId);

    UserResponse convertToUserResponse(User user);
}
