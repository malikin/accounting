package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dto.Transaction;
import com.github.malikin.transferator.dto.TransferOperation;
import com.github.malikin.transferator.service.TransactionService;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import java.util.Set;

@Path("/transaction")
public class TransactionController {

    private final TransactionService transactionService;

    @Inject
    public TransactionController(final TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Path(":operationUuid")
    @GET
    public Result getTransactionByOperationUuid(final String operationUuid) {
        final Set<Transaction> transactions = transactionService.getTransactionByOperationUuid(operationUuid);

        if (transactions.isEmpty()) {
            throw new Err(Status.NOT_FOUND);
        }

        return Results.with(transactions);
    }

    @POST
    public Result makeTransfer(@Body final TransferOperation transferOperation) {
        if (transferOperation.getAmount() <= 0) {
            throw new Err(Status.BAD_REQUEST, "Amount should be greater 0");
        }

        transactionService.makeTransfer(transferOperation);

        return Results.with(Status.CREATED);
    }
}
