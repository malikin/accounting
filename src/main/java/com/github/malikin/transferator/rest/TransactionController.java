package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dao.TransactionRepository;
import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.dto.Transaction;
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

    //Just stub
    @POST
    public Result addTransaction(@Body final Transaction transactionDto) {
        return jdbi.inTransaction(handle -> {
            TransactionRepository transactionRepository = handle.attach(TransactionRepository.class);
            BalanceRepository balanceRepository = handle.attach(BalanceRepository.class);
            AccountRepository accountRepository = handle.attach(AccountRepository.class);

            UUID operationId = UUID.randomUUID();

            Account sender = accountRepository.findAccountById(transactionDto.getSenderId());

            if (sender == null) {
                throw new Err(Status.BAD_REQUEST, "Sender not found");
            }

            Account recipient = accountRepository.findAccountById(transactionDto.getRecipientId());

            if (recipient == null) {
                throw new Err(Status.BAD_REQUEST, "Recipient not found");
            }

            Balance senderBalance = balanceRepository.findBalanceByAccountIdWithLock(transactionDto.getSenderId());
            Balance recipientBalance = balanceRepository.findBalanceByAccountIdWithLock(transactionDto.getRecipientId());

            if (senderBalance.getAmount() < transactionDto.getAmount()) {
                throw new Err(Status.BAD_REQUEST, "Not enough amount on sender balance ");
            }


            //add two transactions
            transactionDto.setOperationUuid(operationId);
            transactionDto.setTimestamp(Instant.now());
            transactionRepository.addTransaction(transactionDto);

            senderBalance.setAmount(senderBalance.getAmount() - transactionDto.getAmount());
            recipientBalance.setAmount(recipientBalance.getAmount() + transactionDto.getAmount());

            balanceRepository.updateBalance(senderBalance);
            balanceRepository.updateBalance(recipientBalance);

            return Results.with(Status.CREATED);
        });
    }
}
