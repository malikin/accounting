package com.github.malikin.transferator.rest;

import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dto.Balance;
import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jooby.Err;
import org.jooby.Status;
import org.jooby.mvc.GET;
import org.jooby.mvc.Path;

@Path("/balance")
public class BalanceController {

    private final Jdbi jdbi;

    @Inject
    public BalanceController(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

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
}
