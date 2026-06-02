package com.test.digitalbankapi.mapper;

import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.entity.Account;
import org.springframework.stereotype.Component;

@Component
public class AccountMapper {
    public AccountResponseDTO toResponseDTO(Account account) {
        return new AccountResponseDTO(account.getId(), account.getOwnerName(), account.getBalance());
    }
}