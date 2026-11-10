package com.minibank.minibank.exception;

public class AccountNotFoundException extends RuntimeException {
    public AccountNotFoundException(Long id) {
        super("Account with id" + id + "not found");
    }
}
