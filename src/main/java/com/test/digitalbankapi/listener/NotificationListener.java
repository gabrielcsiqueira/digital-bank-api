package com.test.digitalbankapi.listener;

import com.test.digitalbankapi.entity.Account;
import com.test.digitalbankapi.entity.Notification;
import com.test.digitalbankapi.entity.Transfer;
import com.test.digitalbankapi.enums.NotificationStatus;
import com.test.digitalbankapi.event.TransferCreatedEvent;
import com.test.digitalbankapi.repository.AccountRepository;
import com.test.digitalbankapi.repository.NotificationRepository;
import com.test.digitalbankapi.repository.TransferRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.LocalDateTime;

@Component
public class NotificationListener {

    private final NotificationRepository notificationRepository;
    private final AccountRepository accountRepository;
    private final TransferRepository transferRepository;

    public NotificationListener(NotificationRepository notificationRepository,
                                AccountRepository accountRepository,
                                TransferRepository transferRepository) {
        this.notificationRepository = notificationRepository;
        this.accountRepository = accountRepository;
        this.transferRepository = transferRepository;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleTransferNotification(TransferCreatedEvent event) {

        Account sourceAccount = accountRepository.getReferenceById(event.sourceAccountId());
        Account destinationAccount = accountRepository.getReferenceById(event.destinationAccountId());
        Transfer transfer = transferRepository.getReferenceById(event.transferId());

        Notification sourceNotification = Notification.builder()
                .account(sourceAccount)
                .transfer(transfer)
                .message(String.format("Transferência de R$ %s realizada com sucesso para a conta ID %d.",
                        event.amount(), event.destinationAccountId()))
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        Notification destNotification = Notification.builder()
                .account(destinationAccount)
                .transfer(transfer)
                .message(String.format("Você recebeu uma transferência de R$ %s da conta ID %d.",
                        event.amount(), event.sourceAccountId()))
                .status(NotificationStatus.SENT)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(sourceNotification);
        notificationRepository.save(destNotification);
    }
}