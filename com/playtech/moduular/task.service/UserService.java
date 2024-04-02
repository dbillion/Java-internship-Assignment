package com.playtech.assignment.moduular.task.service;

import com.playtech.assignment.moduular.task.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> findById(int userId);
    void updateUserBalance(User user, double amount);

    List<User> getAllUsers(); // New method to retrieve all users
}


