package com.playtech.assignment.moduular.task.model;

public class Transaction {
    private String transactionId;
    private int userId;
    private Type type; // Use the enum for type
    private double amount;
    private Method method; // Use the enum for method
    private String accountNumber;

    // Enum for type
    public enum Type {
        DEPOSIT, WITHDRAW;
    }

    // Enum for method
    public enum Method {
        CARD, TRANSFER;
    }

    // Constructor
    public Transaction(String transactionId, int userId, Type type, double amount, Method method, String accountNumber) {
        this.transactionId = transactionId;
        this.userId = userId;
        this.type = type;
        this.amount = amount;
        this.method = method;
        this.accountNumber = accountNumber;
    }

    // Getters and Setters

    public String getTransactionId() {
        return this.transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getUserId() {
        return this.userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public double getAmount() {
        return this.amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public Method getMethod() {
        return this.method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public String getAccountNumber() {
        return this.accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    // ...

    @Override
    public String toString() {
        return "Transaction{" +
                "transactionId='" + transactionId + '\'' +
                ", userId=" + userId +
                ", type=" + type + // Use the enum's name() method for string representation
                ", amount=" + amount +
                ", method=" + method + // Use the enum's name() method for string representation
                ", accountNumber='" + accountNumber + '\'' +
                '}';
    }
}
