package com.github.malikin.rest;

import com.github.malikin.dao.AccountRepository;
import com.github.malikin.dto.Account;
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

import java.util.List;

@Path("/account")
public class AccountController {

    private final DBI dbi;

    @Inject
    public AccountController(final DBI dbi) {
        this.dbi = dbi;
    }

    @Path("/:id")
    @GET
    public Account getAccountById(final Long id) {
        Account account =  dbi.inTransaction((handle, status) -> {
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
        return dbi.inTransaction((handle, status) -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAll();
        });
    }

    @POST
    public Result createAccount(@Body final Account accountDto) {
        return dbi.inTransaction((handle, status) -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            Long id = repository.addAccount(accountDto);
            return Results.with(id, Status.CREATED);
        });
    }
}
