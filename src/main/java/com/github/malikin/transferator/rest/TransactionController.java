package com.github.malikin.transferator.rest;

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
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Path("/transaction")
public class TransactionController {

    private final Jdbi jdbi;

    @Inject
    public TransactionController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Path(":operationUuid")
    @GET
    public Set<Transaction> getTransactionByOperationUuid(final String operationUuid) {
        Set<Transaction> transactions = jdbi.withHandle(handle -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByOperationUuid(operationUuid);
        });

        if (transactions.isEmpty()) {
            throw new Err(Status.NOT_FOUND);
        }

        return transactions;
    }

    @POST
    public Result makeTransfer(@Body final TransferOperation transferOperation) {
        if (transferOperation.getAmount() <= 0) {
            throw new Err(Status.BAD_REQUEST, "Amount should be greater 0");
        }

        return jdbi.inTransaction(handle -> {
            TransactionRepository transactionRepository = handle.attach(TransactionRepository.class);
            BalanceRepository balanceRepository = handle.attach(BalanceRepository.class);
            AccountRepository accountRepository = handle.attach(AccountRepository.class);

            UUID operationUuid = UUID.randomUUID();
            Instant timestamp = Instant.now();

            log.info("Transaction with OperationUUID: {} began", operationUuid);

            Account sender = accountRepository.findAccountById(transferOperation.getSenderId());

            if (sender == null) {
                log.info("Transaction with OperationUUID: {} canceled, sender {} not found",
                        operationUuid, transferOperation.getSenderId());
                throw new Err(Status.BAD_REQUEST, "Sender not found");
            }

            Account recipient = accountRepository.findAccountById(transferOperation.getRecipientId());

            if (recipient == null) {
                log.info("Transaction with OperationUUID: {} canceled, recipient {} not found",
                        operationUuid, transferOperation.getRecipientId());
                throw new Err(Status.BAD_REQUEST, "Recipient not found");
            }

            Balance senderBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getSenderId());
            Balance recipientBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getRecipientId());

            Double amount = transferOperation.getAmount();

            if (senderBalance.getAmount() < amount) {
                log.info("Transaction with OperationUUID: {} canceled, insufficient amount on the sender {} account",
                        operationUuid, transferOperation.getSenderId());
                throw new Err(Status.BAD_REQUEST, "Insufficient amount on the sender account");
            }

            Transaction creditTransaction = Transaction.builder()
                    .amount(-amount)
                    .operationUuid(operationUuid)
                    .senderId(transferOperation.getRecipientId())
                    .recipientId(transferOperation.getSenderId())
                    .timestamp(timestamp)
                    .build();

            transactionRepository.addTransaction(creditTransaction);

            senderBalance.setAmount(senderBalance.getAmount() - amount);
            balanceRepository.updateBalance(senderBalance);

            Transaction debetTransaction = Transaction.builder()
                    .amount(amount)
                    .operationUuid(operationUuid)
                    .senderId(transferOperation.getSenderId())
                    .recipientId(transferOperation.getRecipientId())
                    .timestamp(timestamp)
                    .build();

            transactionRepository.addTransaction(debetTransaction);

            recipientBalance.setAmount(recipientBalance.getAmount() + amount);
            balanceRepository.updateBalance(recipientBalance);

            log.info("Transaction with OperationUUID: {} committed", operationUuid);

            return Results.with(Status.CREATED);
        });
    }
}
