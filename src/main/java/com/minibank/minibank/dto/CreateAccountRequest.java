package com.minibank.minibank.dto;

import com.minibank.minibank.entity.AccountType;
import jakarta.validation.constraints.NotNull;

public record CreateAccountRequest(@NotNull AccountType type) {
}
