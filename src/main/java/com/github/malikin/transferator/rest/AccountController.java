package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.service.AccountService;
import com.google.inject.Inject;
import org.jooby.Err;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

import java.util.Optional;

@Path("/account")
public class AccountController {

    private final AccountService accountService;

    @Inject
    public AccountController(final AccountService accountService) {
        this.accountService = accountService;
    }

    @Path("/:id")
    @GET
    public Result findAccountById(final Long id) {
        Account account = accountService.findAccountById(id);

        if (account == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return Results.with(account);
    }

    @GET
    public Result findAccountByName(final Optional<String> name) {
        if (!name.isPresent()) {
            return Results.with(accountService.findAllAccounts());
        }

        Account account = accountService.findAccountByName(name.get());

        if (account == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return Results.with(account);
    }

    @POST
    public Result createAccount(@Body final Account account) {
        return Results.with(accountService.createAccount(account), Status.CREATED);
    }

    @Path(":accountId/balance")
    @GET
    public Result getBalanceByAccountId(final Long accountId) {
        Balance balance = accountService.getBalanceByAccountId(accountId);
        if (balance == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return Results.with(balance);
    }

    @Path(":accountId/transactions")
    @GET
    public Result getTransactionsByAccountId(final Long accountId) {
        return Results.with(accountService.findTransactionsByAccountId(accountId));
    }
}
