package com.github.malikin.transferator.service;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dao.TransactionRepository;
import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.dto.Transaction;
import com.github.malikin.transferator.dto.TransferOperation;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.jdbi.v3.core.Jdbi;
import org.jooby.Err;
import org.jooby.Status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
public class TransactionService {

    private final Jdbi jdbi;

    @Inject
    public TransactionService(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Set<Transaction> getTransactionByOperationUuid(final String operationUuid) {
        return jdbi.withHandle(handle -> {
            final TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByOperationUuid(operationUuid);
        });
    }

    public void makeTransfer(final TransferOperation transferOperation) {
        if (transferOperation.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new Err(Status.BAD_REQUEST, "Amount should be greater 0");
        }

        jdbi.useTransaction(handle -> {
            final TransactionRepository transactionRepository = handle.attach(TransactionRepository.class);
            final BalanceRepository balanceRepository = handle.attach(BalanceRepository.class);
            final AccountRepository accountRepository = handle.attach(AccountRepository.class);

            final UUID operationUuid = UUID.randomUUID();
            final Instant timestamp = Instant.now();

            log.info("Transaction with OperationUUID: {} began", operationUuid);

            final Account sender = accountRepository.findAccountById(transferOperation.getSenderId());

            if (sender == null) {
                log.info("Transaction with OperationUUID: {} canceled, sender {} not found",
                        operationUuid, transferOperation.getSenderId());
                throw new Err(Status.BAD_REQUEST, "Sender not found");
            }

            final Account recipient = accountRepository.findAccountById(transferOperation.getRecipientId());

            if (recipient == null) {
                log.info("Transaction with OperationUUID: {} canceled, recipient {} not found",
                        operationUuid, transferOperation.getRecipientId());
                throw new Err(Status.BAD_REQUEST, "Recipient not found");
            }

            final Balance senderBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getSenderId());

            final BigDecimal amount = transferOperation.getAmount();

            if (senderBalance.getAmount().compareTo(amount) == -1) {
                log.info("Transaction with OperationUUID: {} canceled, insufficient amount on the sender {} account",
                        operationUuid, transferOperation.getSenderId());
                throw new Err(Status.BAD_REQUEST, "Insufficient amount on the sender account");
            }

            final Transaction creditTransaction = Transaction.builder()
                    .amount(amount.negate())
                    .operationUuid(operationUuid)
                    .senderId(transferOperation.getRecipientId())
                    .recipientId(transferOperation.getSenderId())
                    .timestamp(timestamp)
                    .build();

            transactionRepository.addTransaction(creditTransaction);

            senderBalance.setAmount(senderBalance.getAmount().subtract(amount));
            balanceRepository.updateBalance(senderBalance);

            final Transaction debetTransaction = Transaction.builder()
                    .amount(amount)
                    .operationUuid(operationUuid)
                    .senderId(transferOperation.getSenderId())
                    .recipientId(transferOperation.getRecipientId())
                    .timestamp(timestamp)
                    .build();

            transactionRepository.addTransaction(debetTransaction);

            final Balance recipientBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getRecipientId());

            recipientBalance.setAmount(recipientBalance.getAmount().add(amount));
            balanceRepository.updateBalance(recipientBalance);

            log.info("Transaction with OperationUUID: {} committed", operationUuid);
        });
    }
}
