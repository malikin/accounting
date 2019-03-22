package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dao.TransactionRepository;
import com.github.malikin.transferator.dto.Transaction;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import org.skife.jdbi.v2.DBI;

import java.util.Set;

@Path("/transaction")
public class TransactionController {

    private final DBI dbi;

    @Inject
    public TransactionController(DBI dbi) {
        this.dbi = dbi;
    }

    @Path("/:id")
    @GET
    public Transaction getTransactionById(final Long transactionId) {
        Transaction transaction = dbi.inTransaction((handle, status) -> {
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
        return dbi.inTransaction((handle, status) -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByAccountId(accountId);
        });
    }

    //Just stub
    @POST
    public Result addTransaction(@Body final Transaction transactionDto) {
        return dbi.inTransaction((handle, status) -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            repository.addTransaction(transactionDto);
            return Results.with(Status.CREATED);
        });
    }
}
