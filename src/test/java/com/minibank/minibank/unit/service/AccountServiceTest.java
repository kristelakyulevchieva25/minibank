package com.minibank.minibank.unit.service;

import com.minibank.minibank.dto.AccountResponse;
import com.minibank.minibank.dto.CreateAccountRequest;
import com.minibank.minibank.entity.Account;
import com.minibank.minibank.entity.AccountStatus;
import com.minibank.minibank.entity.AccountType;
import com.minibank.minibank.exception.AccountNotFoundException;
import com.minibank.minibank.repository.AccountRepository;
import com.minibank.minibank.support.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

import com.minibank.minibank.service.AccountService;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataAccessResourceFailureException;

@ExtendWith(MockitoExtension.class)
public class AccountServiceTest {
    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Captor
    private ArgumentCaptor<Account> accountCaptor;

    private Account firstAccount;
    private Account secondAccount;

    @BeforeEach
    void setUp() {
        firstAccount = TestDataFactory.createAccount(1L, "1111111111", AccountType.SAVINGS);
        secondAccount = TestDataFactory.createAccount(2L, "2222222222", AccountType.CHECKING);
    }

    @Test
    void createAccount_whenValidRequest_returnsActiveAccountWithZeroBalance() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(AccountType.SAVINGS);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        verify(accountRepository).save(accountCaptor.capture());
        Account savedAccount = accountCaptor.getValue();
        assertEquals(AccountStatus.ACTIVE, savedAccount.getStatus());
        assertEquals(AccountType.SAVINGS, savedAccount.getType());
        assertEquals(BigDecimal.ZERO, response.balance());
        assertEquals(AccountType.SAVINGS, response.type());
        assertEquals(AccountStatus.ACTIVE, response.status());
        assertEquals(10, response.accountNumber().length());
    }

    @Test
    void getAccount_whenExists_returnsAccount() {
        // given
        when(accountRepository.findById(1L)).thenReturn(Optional.of(firstAccount));

        // when
        AccountResponse response = accountService.getAccount(1L);

        // then
        verify(accountRepository).findById(1L);
        assertEquals(1L, response.id());
        assertEquals("1111111111", response.accountNumber());
        assertEquals(AccountType.SAVINGS, response.type());
    }

    @Test
    void getAccount_whenNotFound_throwsException() {
        // given
        when(accountRepository.findById(999L)).thenReturn(Optional.empty());

        // when
        AccountNotFoundException exception = assertThrows(AccountNotFoundException.class, () -> accountService.getAccount(999L));

        // then
        assertEquals("Account with id 999 not found", exception.getMessage());
    }

    @Test
    void listAccounts_whenAccountsExist_returnsAllAccounts() {
        // given
        when(accountRepository.findAll()).thenReturn(List.of(firstAccount, secondAccount));

        // when
        List<AccountResponse> result = accountService.listAccounts();

        // then
        verify(accountRepository).findAll();
        assertEquals(2, result.size());
        assertEquals("1111111111", result.get(0).accountNumber());
        assertEquals("2222222222", result.get(1).accountNumber());
    }

    @Test
    void createAccount_whenAccountNumberAlreadyExists_generatesAnotherNumber() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(AccountType.CHECKING);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(true, false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        verify(accountRepository, times(2)).existsByAccountNumber(anyString());
        assertEquals(10, response.accountNumber().length());
    }

    @Test
    void listAccounts_whenNoAccounts_returnsEmptyList() {
        // given
        when(accountRepository.findAll()).thenReturn(List.of());

        // when
        List<AccountResponse> result = accountService.listAccounts();

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    void createAccount_whenDatabaseFails_throwsException() {
        // given
        CreateAccountRequest request = new CreateAccountRequest(AccountType.SAVINGS);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenThrow(new DataAccessResourceFailureException("Database is down"));

        // when and then
        assertThrows(DataAccessException.class, () -> accountService.createAccount(request));
    }

    @ParameterizedTest
    @EnumSource(AccountType.class)
    void createAccount_forEveryAccountType_createsActiveAccount(AccountType type) {
        // given
        CreateAccountRequest request = new CreateAccountRequest(type);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        AccountResponse response = accountService.createAccount(request);

        // then
        assertEquals(type, response.type());
        assertEquals(AccountStatus.ACTIVE, response.status());
    }


    @Test
    void listAccounts_whenDatabaseFails_throwsException() {
        // given
        when(accountRepository.findAll()).thenThrow(new DataAccessResourceFailureException("Database is down"));

        // when and then
        assertThrows(DataAccessException.class, () -> accountService.listAccounts());
    }
}
