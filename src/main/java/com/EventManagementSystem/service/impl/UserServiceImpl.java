package com.EventManagementSystem.service.impl;

import java.util.List;

import com.EventManagementSystem.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.EventManagementSystem.exception.UserNotFoundException;
import com.EventManagementSystem.exception.ValidationException;
import com.EventManagementSystem.model.Role;
import com.EventManagementSystem.model.User;
import com.EventManagementSystem.repository.UserRepository;
import com.EventManagementSystem.service.AccountService;
import com.EventManagementSystem.service.UserService;
import com.EventManagementSystem.util.PasswordUtil;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final AccountService accountService;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, AccountService accountService) {
        this.userRepository = userRepository;
        this.accountService = accountService;
    }

    @Override
    public User register(String name, String email, String phone, String password, Role role) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new ValidationException("A user with this email already exists"+existing);
        });

        String hashedPassword = PasswordUtil.hashPassword(password);

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(hashedPassword);
        user.setRole(role);
        user.setStatus(true);

        User created = userRepository.save(user);

        if (role == Role.ORGANIZER) {
            accountService.createAccountForOrganizer(created.getId());
        }

        return created;
    }

    @Override
    public User login(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("No account found with this email"));

        if (user.getStatus()) {
            throw new ValidationException("This account has been deactivated, please contact admin");
        }

        if (!PasswordUtil.matches(password, user.getPasswordHash())) {
            throw new ValidationException("Incorrect password");
        }

        return user;
    }

    @Override
    public User getProfile(long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
    }

    @Override
    public void updateProfile(long userId, String name, String phone) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));

        if (name == null || name.trim().isEmpty()) {
            throw new ValidationException("Name cannot be empty");
        }

        user.setName(name);
        user.setPhone(phone);
        userRepository.save(user);
    }

    @Override
    public List<User> listUsersByRole(Role role) {
        return userRepository.findByRole(role);
    }

    @Override
    public List<User> listAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public void deactivateUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
        user.setStatus(false);
        userRepository.save(user);
    }

    @Override
    public void activateUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
        user.setStatus(true);
        userRepository.save(user);
    }

    @Override
    public long countByRole(Role role) {
        return userRepository.countByRole(role);
    }

    @Override
    public void deleteUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
        userRepository.delete(user);
    }

    @Override
    public UserResponse convertToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setName(user.getName());
        response.setEmail(user.getEmail());
        response.setPhone(user.getPhone());
        response.setEventCount(user.getEventCount());
        response.setRole(user.getRole());
        response.setStatus(user.getStatus());
        return response;
    }

}
