package com.playtech.assignment.moduular.task.service;

import com.playtech.assignment.moduular.task.model.User;

import java.util.List;
import java.util.Optional;

public class UserServiceWork implements  UserService{

    @Override
    public Optional<User> findById(int userId) {
        return Optional.empty();
    }

    @Override
    public void updateUserBalance(User user, double amount) {

    }

    @Override
    public List<User> getAllUsers() {
        return null;
    }
}