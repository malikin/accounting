package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dao.TransactionRepository;
import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.dto.Transaction;
import com.github.malikin.transferator.dto.TransferOperation;
import com.google.inject.Inject;
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

@Path("/transaction")
public class TransactionController {

    private final Jdbi jdbi;

    @Inject
    public TransactionController(Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Path("/:id")
    @GET
    public Transaction getTransactionById(final Long transactionId) {
        Transaction transaction = jdbi.inTransaction(handle -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionById(transactionId);
        });

        if (transaction == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return transaction;
    }

    @GET
    public Set<Transaction> getTransactionsByAccountId(final Long accountId) {
        return jdbi.inTransaction(handle -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByAccountId(accountId);
        });
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

            Account sender = accountRepository.findAccountById(transferOperation.getSenderId());

            if (sender == null) {
                throw new Err(Status.BAD_REQUEST, "Sender not found");
            }

            Account recipient = accountRepository.findAccountById(transferOperation.getRecipientId());

            if (recipient == null) {
                throw new Err(Status.BAD_REQUEST, "Recipient not found");
            }

            Balance senderBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getSenderId());
            Balance recipientBalance = balanceRepository.findBalanceByAccountIdWithLock(transferOperation.getRecipientId());

            Double amount = transferOperation.getAmount();

            if (senderBalance.getAmount() < amount) {
                throw new Err(Status.BAD_REQUEST, "Not enough amount on sender balance ");
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

            return Results.with(Status.CREATED);
        });
    }
}
