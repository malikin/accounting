package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dto.Account;
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

import java.util.List;

@Path("/account")
public class AccountController {

    private final Jdbi jdbi;

    @Inject
    public AccountController(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    @Path("/:id")
    @GET
    public Account getAccountById(final Long id) {
        Account account =  jdbi.inTransaction(handle -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAccountById(id);
        });

        if (account == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return account;
    }

    @GET
    public List<Account> getAllAccounts() {
        return jdbi.inTransaction(handle -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAll();
        });
    }

    @POST
    public Result createAccount(@Body final Account accountDto) {
        return jdbi.inTransaction(handle -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            Long id = repository.addAccount(accountDto);
            return Results.with(id, Status.CREATED);
        });
    }
}
