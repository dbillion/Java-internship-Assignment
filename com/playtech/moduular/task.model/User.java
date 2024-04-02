package com.playtech.assignment.moduular.task.model;

public class User {
private int userId;
private String username;
private double balance;
private String country;
private FrozenStatus frozen;
private double depositMin;
private double depositMax;
private double withdrawMin;
private double withdrawMax;

// Enum for frozen status
public enum FrozenStatus {
    ACTIVE_USER(0), FROZEN_USER(1);

    private final int value;

    FrozenStatus(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static FrozenStatus fromValue(int value) {
        for (FrozenStatus status : FrozenStatus.values()) {
            if (status.getValue() == value) {
                return status;
            }
        }
        throw new IllegalArgumentException("No enum constant for value: " + value);
    }
}

// Constructor
public User(int userId, String username, double balance, String country, FrozenStatus frozen2, double depositMin, double depositMax, double withdrawMin, double withdrawMax) {
    this.userId = userId;
    this.username = username;
    this.balance = balance;
    this.country = country;
    this.frozen = frozen2;
    this.depositMin = depositMin;
    this.depositMax = depositMax;
    this.withdrawMin = withdrawMin;
    this.withdrawMax = withdrawMax;
}


public boolean isFrozen() {
    return this.frozen == FrozenStatus.FROZEN_USER;
}

public void deposit(double amount) {
    this.balance += amount;
}

public void withdraw(double amount) {
    this.balance -= amount;
}

// Getters and Setters

public int getUserId() {
    return this.userId;
}

public void setUserId(int userId) {
    this.userId = userId;
}

public String getUsername() {
    return this.username;
}

public void setUsername(String username) {
    this.username = username;
}

public double getBalance() {
    return this.balance;
}

public void setBalance(double balance) {
    this.balance = balance;
}

public String getCountry() {
    return this.country;
}

public void setCountry(String country) {
    this.country = country;
}

public FrozenStatus getFrozen() {
    return this.frozen;
}

public void setFrozen(FrozenStatus frozen) {
    this.frozen = frozen;
}

public double getDepositMin() {
    return this.depositMin;
}

public void setDepositMin(double depositMin) {
    this.depositMin = depositMin;
}

public double getDepositMax() {
    return this.depositMax;
}

public void setDepositMax(double depositMax) {
    this.depositMax = depositMax;
}

public double getWithdrawMin() {
    return this.withdrawMin;
}

public void setWithdrawMin(double withdrawMin) {
    this.withdrawMin = withdrawMin;
}

public double getWithdrawMax() {
    return this.withdrawMax;
}

public void setWithdrawMax(double withdrawMax) {
    this.withdrawMax = withdrawMax;
}

// ...

@Override
public String toString() {
    return "User{" +
            "userId=" + userId +
            ", username='" + username + '\'' +
            ", balance=" + balance +
            ", country='" + country + '\'' +
            ", frozen=" + frozen +
            ", depositMin=" + depositMin +
            ", depositMax=" + depositMax +
            ", withdrawMin=" + withdrawMin +
            ", withdrawMax=" + withdrawMax +
            '}';
}
}

