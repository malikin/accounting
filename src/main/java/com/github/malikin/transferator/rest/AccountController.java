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

import java.util.Optional;
import java.util.Set;

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
    public Result getAccountByName(final Optional<String> name) {

        if (!name.isPresent()) {
            return jdbi.inTransaction(handle -> {
                AccountRepository repository = handle.attach(AccountRepository.class);
                return Results.with(repository.findAll());
            });
        }

        Account account =  jdbi.inTransaction(handle -> {
            AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAccountByName(name.get());
        });

        if (account == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return Results.with(account);
    }

    @POST
    public Result createAccount(@Body final Account account) {
        return jdbi.inTransaction(handle -> {
            AccountRepository accountRepository = handle.attach(AccountRepository.class);
            BalanceRepository balanceRepository = handle.attach(BalanceRepository.class);

            Account existedAccount = accountRepository.findAccountByName(account.getName());

            if (existedAccount != null) {
                throw new Err(Status.BAD_REQUEST, String.format("Account with name %s already exist", account.getName()));
            }

            Long id = accountRepository.addAccount(account);
            balanceRepository.addBalance(new Balance(id, 0.0));

            return Results.with(accountRepository.findAccountById(id), Status.CREATED);
        });
    }

    @Path(":accountId/balance")
    @GET
    public Balance getBalanceByAccountId(final Long accountId) {
        Balance balance = jdbi.inTransaction(handle -> {
            BalanceRepository repository = handle.attach(BalanceRepository.class);
            return repository.findBalanceByAccountId(accountId);
        });

        if (balance == null) {
            throw new Err(Status.NOT_FOUND);
        }

        return balance;
    }

    @Path(":accountId/transactions")
    @GET
    public Set<Transaction> getTransactionsByAccountId(final Long accountId) {
        return jdbi.inTransaction(handle -> {
            TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByAccountId(accountId);
        });
    }
}
