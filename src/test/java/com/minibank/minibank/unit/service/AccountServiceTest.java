package com.minibank.minibank.unit.service;

import com.minibank.minibank.dto.AccountResponse;
import com.minibank.minibank.dto.CreateAccountRequest;
import com.minibank.minibank.entity.Account;
import com.minibank.minibank.entity.AccountStatus;
import com.minibank.minibank.entity.AccountType;
import com.minibank.minibank.exception.AccountNotFoundException;
import com.minibank.minibank.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import com.minibank.minibank.service.AccountService;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void createAccount_returnsActiveAccountWithZeroBalance() {
        CreateAccountRequest request = new CreateAccountRequest(AccountType.SAVINGS);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AccountResponse response = accountService.createAccount(request);

        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(AccountType.SAVINGS, response.type());
        assertEquals(AccountStatus.ACTIVE, response.status());
        assertEquals(10, response.accountNumber().length());
    }

    @Test
    void getAccount_whenExists_returnAccount() {
        Account account = new Account();
        account.setId(1L);
        account.setAccountNumber("1234567890");
        account.setBalance(BigDecimal.ZERO);
        account.setType(AccountType.CHECKING);
        account.setStatus(AccountStatus.ACTIVE);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        AccountResponse response = accountService.getAccount(1L);

        assertEquals(1L, response.id());
        assertEquals("1234567890", response.accountNumber());
        assertEquals(AccountType.CHECKING, response.type());
    }

    @Test
    void getAccount_whenNotFound_throwsException() {
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.getAccount(999L));
    }

    @Test
    void listAccounts_returnsAllAccounts() {
        Account first = new Account();
        first.setId(1L);
        first.setAccountNumber("1111111111");
        first.setBalance(BigDecimal.ZERO);
        first.setType(AccountType.SAVINGS);
        first.setStatus(AccountStatus.ACTIVE);

        Account second = new Account();
        second.setId(2L);
        second.setAccountNumber("2222222222");
        second.setBalance(BigDecimal.ZERO);
        second.setType(AccountType.CHECKING);
        second.setStatus(AccountStatus.ACTIVE);

        when(accountRepository.findAll()).thenReturn(List.of(first, second));

        List<AccountResponse> result = accountService.listAccounts();

        assertEquals(2, result.size());
        assertEquals("1111111111", result.get(0).accountNumber());
        assertEquals("2222222222", result.get(1).accountNumber());

    }

}
