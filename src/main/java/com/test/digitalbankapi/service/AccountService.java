package com.test.digitalbankapi.service;

import com.test.digitalbankapi.dto.request.AccountRequestDTO;
import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.repository.AccountRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponseDTO create(AccountRequestDTO request) {
        Account account = Account.builder()
                .ownerName(request.ownerName().trim())
                .balance(request.initialBalance())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);

        return new AccountResponseDTO(saved.getId(), saved.getOwnerName(), saved.getBalance());
    }

    public List<AccountResponseDTO> findAll() {
        return accountRepository.findAll()
                .stream()
                .map(acc -> new AccountResponseDTO(
                        acc.getId(),
                        acc.getOwnerName(),
                        acc.getBalance()
                ))
                .toList();
    }

}
