package com.minibank.minibank.service;

import com.minibank.minibank.dto.AccountResponse;
import com.minibank.minibank.dto.CreateAccountRequest;
import com.minibank.minibank.entity.Account;
import com.minibank.minibank.entity.AccountStatus;
import com.minibank.minibank.exception.AccountNotFoundException;
import com.minibank.minibank.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private static final long MIN_ACCOUNT_NUMBER = 1_000_000_000L;
    private static final long MAX_ACCOUNT_NUMBER = 10_000_000_000L; // upper bound is exclusive
    private final AccountRepository accountRepository;

    public AccountResponse createAccount(CreateAccountRequest request) throws DataAccessException{
       try {
           Account account = new Account();
           account.setAccountNumber(generateAccountNumber());
           account.setBalance(BigDecimal.ZERO);
           account.setType(request.type());
           account.setStatus(AccountStatus.ACTIVE);
           Account savedAccount = accountRepository.save(account);
           log.info("Account {} created with type {}", savedAccount.getAccountNumber(), savedAccount.getType());
           return AccountResponse.from(savedAccount);
       } catch (DataAccessException e) {
           log.error("Database error while creating account of type {}", request.type(), e);
           throw e;
       }
    }

    public AccountResponse getAccount(Long id) throws AccountNotFoundException {
       try {
           Account account = accountRepository.findById(id)
                   .orElseThrow(() -> new AccountNotFoundException(id));
           return AccountResponse.from(account);
       } catch (AccountNotFoundException e) {
           log.warn("Account with id {} not found", id);
           throw e;
       }
    }

    public List<AccountResponse> listAccounts() throws DataAccessException {
       try {
           return accountRepository.findAll().stream()
                   .map(AccountResponse::from)
                   .toList();
       } catch (DataAccessException e) {
           log.error("Database error while listing accounts", e);
           throw e;
       }
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = String.valueOf(
                    ThreadLocalRandom.current().nextLong(MIN_ACCOUNT_NUMBER, MAX_ACCOUNT_NUMBER));
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;

    }
}