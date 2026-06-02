package com.test.digitalbankapi.service;

import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.dto.response.TransferResponseDTO;
import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.entity.Transfer;
import com.test.digitalbankapi.enums.TransferStatus;
import com.test.digitalbankapi.event.TransferCreatedEvent;
import com.test.digitalbankapi.exception.AccountNotFoundException;
import com.test.digitalbankapi.exception.InsufficientBalanceException;
import com.test.digitalbankapi.exception.InvalidTransferAmountException;
import com.test.digitalbankapi.exception.SameAccountTransferException;
import com.test.digitalbankapi.mapper.TransferMapper;
import com.test.digitalbankapi.repository.AccountRepository;
import com.test.digitalbankapi.repository.TransferRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);

    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;
    private final TransferMapper transferMapper;
    private final ApplicationEventPublisher eventPublisher;

    public TransferService(AccountRepository accountRepository,
                           TransferRepository transferRepository,
                           TransferMapper transferMapper,
                           ApplicationEventPublisher applicationEventPublisher) {
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
        this.transferMapper = transferMapper;
        this.eventPublisher = applicationEventPublisher;
    }

    @Transactional
    public TransferResponseDTO transfer(@Valid TransferRequestDTO request) {
        log.info("Processing transfer request. Source Account: {}, Destination Account: {}, Amount: {}",
                request.sourceAccountId(), request.destinationAccountId(), request.amount());

        validateRequest(request);

        log.debug("Acquiring pessimistic locks for accounts...");
        LockedAccounts accounts = lockAndFetchAccounts(request.sourceAccountId(), request.destinationAccountId());

        log.debug("Executing monetary transaction steps...");
        executeMonetaryTransaction(accounts.source(), accounts.destination(), request.amount());

        Transfer savedTransfer = recordTransferHistory(accounts.source(), accounts.destination(), request.amount());

        log.debug("Publishing TransferCreatedEvent for transfer ID: {}", savedTransfer.getId());
        eventPublisher.publishEvent(new TransferCreatedEvent(
                savedTransfer.getId(),
                accounts.source().getId(),
                accounts.destination().getId(),
                request.amount()
        ));

        log.info("Transfer ID {} completed successfully.", savedTransfer.getId());
        return transferMapper.toResponseDTO(savedTransfer);
    }

    private void validateRequest(TransferRequestDTO request) {
        if (request.sourceAccountId().equals(request.destinationAccountId())) {
            log.warn("Transfer validation failed: Source and destination accounts are identical (ID: {})", request.sourceAccountId());
            throw new SameAccountTransferException();
        }
        if (request.amount() == null || request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            log.warn("Transfer validation failed: Invalid transfer amount: {}", request.amount());
            throw new InvalidTransferAmountException();
        }
    }

    private LockedAccounts lockAndFetchAccounts(Long sourceId, Long destinationId) {
        Long firstId = Math.min(sourceId, destinationId);
        Long secondId = Math.max(sourceId, destinationId);

        Account firstAccount = accountRepository.findByIdForUpdate(firstId)
                .orElseThrow(() -> {
                    log.error("Account ID {} not found during lock acquisition", firstId);
                    return new AccountNotFoundException(firstId);
                });

        Account secondAccount = accountRepository.findByIdForUpdate(secondId)
                .orElseThrow(() -> {
                    log.error("Account ID {} not found during lock acquisition", secondId);
                    return new AccountNotFoundException(secondId);
                });

        Account source = firstAccount.getId().equals(sourceId) ? firstAccount : secondAccount;
        Account destination = firstAccount.getId().equals(sourceId) ? secondAccount : firstAccount;

        return new LockedAccounts(source, destination);
    }

    private void executeMonetaryTransaction(Account source, Account destination, BigDecimal amount) {
        if (source.getBalance().compareTo(amount) < 0) {
            log.warn("Transaction rejected: Account ID {} has insufficient balance. Available: {}, Requested: {}",
                    source.getId(), source.getBalance(), amount);
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