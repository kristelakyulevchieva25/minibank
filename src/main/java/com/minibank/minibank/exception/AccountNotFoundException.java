package com.minibank.minibank.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Thrown when an account with the given id does not exist.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {
    /**
     * Creates the exception with a message that contains the missing account id.
     *
     * @param id the id of the account that was not found
     */
    public AccountNotFoundException(Long id) {
        super("Account with id " + id + " not found");
    }
}