package com.test.digitalbankapi.service;

import com.test.digitalbankapi.dto.request.AccountRequestDTO;
import com.test.digitalbankapi.dto.response.AccountResponseDTO;
import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.mapper.AccountMapper;
import com.test.digitalbankapi.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {

    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.accountMapper = accountMapper;
    }

    public AccountResponseDTO create(AccountRequestDTO request) {
        log.info("Initiating creation of a new account for owner: {}", request.ownerName());

        Account account = Account.builder()
                .ownerName(request.ownerName().trim())
                .balance(request.initialBalance())
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        Account saved = accountRepository.save(account);

        log.info("Account successfully created with ID: {}", saved.getId());
        return accountMapper.toResponseDTO(saved);
    }

    public List<AccountResponseDTO> findAll() {
        log.debug("Fetching all accounts ordered by ID");
        return accountRepository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(accountMapper::toResponseDTO)
                .toList();
    }
}