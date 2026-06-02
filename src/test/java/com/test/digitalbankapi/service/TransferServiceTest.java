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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private TransferService transferService;

    private Account sourceAccount;
    private Account destinationAccount;

    @BeforeEach
    void setUp() {
        sourceAccount = Account.builder()
                .id(1L)
                .ownerName("Gabriel Siqueira")
                .balance(new BigDecimal("1000.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        destinationAccount = Account.builder()
                .id(2L)
                .ownerName("João Silva")
                .balance(new BigDecimal("500.00"))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    @Test
    @DisplayName("Should execute transfer successfully when request is valid")
    void transferSuccess() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("200.00"));

        Transfer mockTransfer = Transfer.builder()
                .id(100L)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(request.amount())
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        TransferResponseDTO expectedResponse = new TransferResponseDTO(
                100L, 1L, 2L, request.amount(), mockTransfer.getCreatedAt()
        );

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destinationAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(mockTransfer);
        when(transferMapper.toResponseDTO(mockTransfer)).thenReturn(expectedResponse);

        TransferResponseDTO response = transferService.transfer(request);

        assertNotNull(response);
        assertEquals(100L, response.transferId());
        assertEquals(new BigDecimal("800.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("700.00"), destinationAccount.getBalance());

        ArgumentCaptor<Transfer> transferCaptor = ArgumentCaptor.forClass(Transfer.class);
        verify(transferRepository).save(transferCaptor.capture());
        assertEquals(TransferStatus.SUCCESS, transferCaptor.getValue().getStatus());

        verify(accountRepository, times(2)).findByIdForUpdate(anyLong());
        verify(eventPublisher, times(1)).publishEvent(any(com.test.digitalbankapi.event.TransferCreatedEvent.class));
    }

    @Test
    @DisplayName("Should throw SameAccountTransferException when source and destination IDs are identical")
    void transferThrowsSameAccountException() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 1L, new BigDecimal("100.00"));

        assertThrows(SameAccountTransferException.class, () -> transferService.transfer(request));

        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    @DisplayName("Should throw InvalidTransferAmountException when amount is negative or zero")
    void transferThrowsInvalidAmountException() {
        TransferRequestDTO requestZero = new TransferRequestDTO(1L, 2L, BigDecimal.ZERO);
        TransferRequestDTO requestNegative = new TransferRequestDTO(1L, 2L, new BigDecimal("-50.00"));

        assertThrows(InvalidTransferAmountException.class, () -> transferService.transfer(requestZero));
        assertThrows(InvalidTransferAmountException.class, () -> transferService.transfer(requestNegative));

        verifyNoInteractions(accountRepository, transferRepository);
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when source account does not exist")
    void transferThrowsSourceAccountNotFound() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw AccountNotFoundException when destination account does not exist")
    void transferThrowsDestinationAccountNotFound() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class, () -> transferService.transfer(request));
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw InsufficientBalanceException when source account has less money than requested")
    void transferThrowsInsufficientBalance() {
        BigDecimal expensiveAmount = new BigDecimal("1500.00");
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, expensiveAmount);

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destinationAccount));

        assertThrows(InsufficientBalanceException.class, () -> transferService.transfer(request));

        assertEquals(new BigDecimal("1000.00"), sourceAccount.getBalance());
        assertEquals(new BigDecimal("500.00"), destinationAccount.getBalance());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should execute transfer successfully when amount is exactly equal to the available balance")
    void transferWithExactBalance() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("1000.00"));

        Transfer mockTransfer = Transfer.builder()
                .id(101L)
                .sourceAccount(sourceAccount)
                .destinationAccount(destinationAccount)
                .amount(request.amount())
                .status(TransferStatus.SUCCESS)
                .createdAt(LocalDateTime.now())
                .build();

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destinationAccount));
        when(transferRepository.save(any(Transfer.class))).thenReturn(mockTransfer);
        when(transferMapper.toResponseDTO(mockTransfer)).thenReturn(new TransferResponseDTO(101L, 1L, 2L, request.amount(), mockTransfer.getCreatedAt()));

        transferService.transfer(request);

        assertEquals(0, BigDecimal.ZERO.compareTo(sourceAccount.getBalance()));
        assertEquals(0, new BigDecimal("1500.00").compareTo(destinationAccount.getBalance()));

        verify(eventPublisher, times(1)).publishEvent(any(com.test.digitalbankapi.event.TransferCreatedEvent.class));
    }

    @Test
    @DisplayName("Should rollback account balances if transfer saving fails unexpectedly")
    void shouldRollbackWhenUnexpectedExceptionOccurs() {
        TransferRequestDTO request = new TransferRequestDTO(1L, 2L, new BigDecimal("100.00"));

        when(accountRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(destinationAccount));

        when(transferRepository.save(any(Transfer.class)))
                .thenThrow(new RuntimeException("Database unexpected connection failure"));

        assertThrows(RuntimeException.class, () -> transferService.transfer(request));

        assertNotNull(sourceAccount.getBalance());
    }
}