package com.test.digitalbankapi.service;

import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.entity.Transfer;
import com.test.digitalbankapi.enums.TransferStatus;
import com.test.digitalbankapi.exception.AccountNotFoundException;
import com.test.digitalbankapi.exception.InsufficientBalanceException;
import com.test.digitalbankapi.exception.InvalidTransferAmountException;
import com.test.digitalbankapi.exception.SameAccountTransferException;
import com.test.digitalbankapi.mapper.TransferMapper;
import com.test.digitalbankapi.repository.AccountRepository;
import com.test.digitalbankapi.repository.TransferRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;

    public TransferService(AccountRepository accountRepository,
                           TransferRepository transferRepository,
                           TransferMapper transferMapper) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
    }

    @Transactional
    public TransferResponseDTO transfer(@Valid TransferRequestDTO request) {
        validateRequest(request);

        LockedAccounts accounts = lockAndFetchAccounts(request.sourceAccountId(), request.destinationAccountId());

        executeMonetaryTransaction(accounts.source(), accounts.destination(), request.amount());

        Transfer savedTransfer = recordTransferHistory(accounts.source(), accounts.destination(), request.amount());

        return transferMapper.toResponseDTO(savedTransfer);
    }

    private void validateRequest(TransferRequestDTO request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            throw new SameAccountTransferException();
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransferAmountException();
        }
    }

    private LockedAccounts lockAndFetchAccounts(Long sourceId, Long destinationId) {
        Long firstId = Math.min(sourceId, destinationId);
        Long secondId = Math.max(sourceId, destinationId);

        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> new AccountNotFoundException(firstId));

        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> new AccountNotFoundException(secondId));

        Account source = firstAccount.getId().equals(sourceId) ? firstAccount : secondAccount;
        Account destination = firstAccount.getId().equals(sourceId) ? secondAccount : firstAccount;

        return new LockedAccounts(source, destination);
    }

    private void executeMonetaryTransaction(Account source, Account destination, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(source.getBalance(), amount);
        }

        LocalDateTime now = LocalDateTime.now();

        source.setBalance(source.getBalance().subtract(amount));
        source.setUpdatedAt(now);

        destination.setBalance(destination.getBalance().add(amount));
        destination.setUpdatedAt(now);
    }

    private Transfer recordTransferHistory(Account source, Account destination, BigDecimal amount) {
        Transfer transfer = Transfer.builder()
                .sourceAccount(source)
                .destinationAccount(destination)
                .amount(amount)
                .createdAt(LocalDateTime.now())
                .status(TransferStatus.SUCCESS)
                .build();

        return transferRepository.save(transfer);
    }

    private record LockedAccounts(Account source, Account destination) {}
}