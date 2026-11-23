package com.minibank.minibank.unit.controller;

import com.minibank.minibank.controller.AccountController;
import com.minibank.minibank.dto.AccountResponse;
import com.minibank.minibank.entity.AccountStatus;
import com.minibank.minibank.entity.AccountType;
import com.minibank.minibank.exception.AccountNotFoundException;
import com.minibank.minibank.service.AccountService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AccountController.class)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AccountService accountService;

    @Test
    void createAccount_whenValidRequest_returns201() throws Exception {
        // given
        when(accountService.createAccount(any())).thenReturn(
                new AccountResponse(1L, "1234567890", BigDecimal.ZERO,
                        AccountType.SAVINGS, AccountStatus.ACTIVE));

        // when and then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAVINGS\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accountNumber").value("1234567890"))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    void createAccount_whenTypeIsMissing_returns400() throws Exception {
        // when and then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAccount_whenNotFound_returns404WithMessage() throws Exception {
        // given
        when(accountService.getAccount(999L)).thenThrow(new AccountNotFoundException(999L));

        // when and then
        mockMvc.perform(get("/accounts/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Account with id 999 not found"));
    }

    @Test
    void createAccount_whenServiceFails_returns500WithGeneralMessage() throws Exception {
        // given
        when(accountService.createAccount(any()))
                .thenThrow(new DataAccessResourceFailureException("Database is down"));

        // when and then
        mockMvc.perform(post("/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"type\":\"SAVINGS\"}"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("Something went wrong while creating the account"));
    }
}