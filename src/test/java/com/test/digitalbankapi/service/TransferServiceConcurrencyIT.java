package com.test.digitalbankapi.service;

import com.test.digitalbankapi.dto.request.TransferRequestDTO;
import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.repository.AccountRepository;
import com.test.digitalbankapi.repository.TransferRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class TransferServiceConcurrencyIT {

    @ServiceConnection
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    static {
        postgres.start();
    }

    @Autowired
    private TransferService transferService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransferRepository transferRepository;

    private Long accountAId;
    private Long accountBId;

    @BeforeEach
    void setUp() {
        transferRepository.deleteAll();
        accountRepository.deleteAll();

        Account accountA = accountRepository.save(Account.builder()
                .ownerName("Account A")
                .balance(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        Account accountB = accountRepository.save(Account.builder()
                .ownerName("Account B")
                .balance(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build());

        accountAId = accountA.getId();
        accountBId = accountB.getId();
    }

    @Test
    @DisplayName("Should maintain data consistency during high concurrent transfers using real PostgreSQL")
    void shouldHandleHighConcurrencySuccessfully() throws InterruptedException {
        int numberOfThreads = 100;
        BigDecimal transferAmount = new BigDecimal("10.00");

        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < numberOfThreads; i++) {
            service.execute(() -> {
                try {
                    latch.await();
                    TransferRequestDTO request = new TransferRequestDTO(accountAId, accountBId, transferAmount);
                    transferService.transfer(request);
                } catch (Exception e) {
                    System.err.println("Transaction failed: " + e.getMessage());
                }
            });
        }

        latch.countDown();
        service.shutdown();

        boolean finishedCleanly = service.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(finishedCleanly, "O teste demorou demais e estourou o timeout");

        Account finalAccountA = accountRepository.findById(accountAId).orElseThrow();
        Account finalAccountB = accountRepository.findById(accountBId).orElseThrow();

        assertEquals(0, new BigDecimal("0.00").compareTo(finalAccountA.getBalance()));
        assertEquals(0, new BigDecimal("2000.00").compareTo(finalAccountB.getBalance()));
        assertEquals(100, transferRepository.count());
    }

    @Test
    @DisplayName("Should prevent deadlocks when concurrent transfers happen bi-directionally between same accounts")
    void shouldHandleBiDirectionalTransfersWithoutDeadlock() throws InterruptedException {
        int numberOfThreads = 40;
        ExecutorService service = Executors.newFixedThreadPool(numberOfThreads);
        CountDownLatch latch = new CountDownLatch(1);

        for (int i = 0; i < numberOfThreads; i++) {
            final boolean alternation = (i % 2 == 0);
            service.execute(() -> {
                try {
                    latch.await();
                    TransferRequestDTO request = alternation
                            ? new TransferRequestDTO(accountAId, accountBId, new BigDecimal("5.00"))
                            : new TransferRequestDTO(accountBId, accountAId, new BigDecimal("5.00"));

                    transferService.transfer(request);
                } catch (Exception e) {
                    System.err.println("Transaction failed during stress: " + e.getMessage());
                }
            });
        }

        latch.countDown();
        service.shutdown();

        boolean finishedCleanly = service.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(finishedCleanly, "O teste de deadlock estourou o timeout");

        Account finalAccountA = accountRepository.findById(accountAId).orElseThrow();
        Account finalAccountB = accountRepository.findById(accountBId).orElseThrow();

        assertEquals(0, new BigDecimal("1000.00").compareTo(finalAccountA.getBalance()), "Conta A divergiu!");
        assertEquals(0, new BigDecimal("1000.00").compareTo(finalAccountB.getBalance()), "Conta B divergiu!");
    }
}