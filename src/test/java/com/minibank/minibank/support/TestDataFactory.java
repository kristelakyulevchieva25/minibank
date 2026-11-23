package com.minibank.minibank.support;

import com.minibank.minibank.entity.Account;
import com.minibank.minibank.entity.AccountStatus;
import com.minibank.minibank.entity.AccountType;

import java.math.BigDecimal;

/**
 *Creates test data objects so tests do not repeat the same setup code.
 */
public final class TestDataFactory {

    private TestDataFactory(){
    }

    /**
     * Creates an active account with zero balance.
     */
    public static Account createAccount(Long id, String accountNumber, AccountType type) {
        Account account = new Account();
        account.setId(id);
        account.setAccountNumber(accountNumber);
        account.setBalance(BigDecimal.ZERO);
        account.setType(type);
        account.setStatus(AccountStatus.ACTIVE);
        return account;
    }
}
