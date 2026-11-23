package com.minibank.minibank.controller;

import com.minibank.minibank.dto.AccountResponse;
import com.minibank.minibank.dto.CreateAccountRequest;
import com.minibank.minibank.dto.ErrorResponse;
import com.minibank.minibank.exception.AccountNotFoundException;
import com.minibank.minibank.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for creating and reading bank accounts.
 */
@Slf4j
@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates a new account.
     *
     * @param request the request containing the account type
     * @return 201 with the created account, or 500 if something goes wrong
     */
    @PostMapping
    public ResponseEntity<?> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        log.info("Request to create account of type {}", request.type());
        try {
            AccountResponse response = accountService.createAccount(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Unexpected error while creating account", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong while creating the account"));
        }
    }

    /**
     * Returns an account by its id.
     *
     * @param id the id of the account
     * @return 200 with the account, 404 if it does not exist, or 500 if something goes wrong
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getAccount(@PathVariable Long id) {
        log.info("Request to get account with id {}", id);
        try {
            return ResponseEntity.ok(accountService.getAccount(id));
        } catch (AccountNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            log.error("Unexpected error while getting account {}", id, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong while getting the account"));
        }
    }

    /**
     * Returns all accounts.
     *
     * @return 200 with the list of accounts, or 500 if something goes wrong
     */
    @GetMapping
    public ResponseEntity<?> listAccounts() {
        log.info("Request to list all accounts");
        try {
            return ResponseEntity.ok(accountService.listAccounts());
        } catch (Exception e) {
            log.error("Unexpected error while listing accounts", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Something went wrong while listing the accounts"));
        }
    }
}