package com.github.malikin.rest;

import com.github.malikin.dao.BalanceRepository;
import com.github.malikin.dto.Balance;
import com.google.inject.Inject;
import org.jooby.Result;
import org.jooby.Results;
import org.jooby.Status;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import org.skife.jdbi.v2.DBI;

@Path("/balance")
public class BalanceController {

    private final DBI dbi;

    @Inject
    public BalanceController(final DBI dbi) {
        this.dbi = dbi;
    }

    @Path("/:userId")
    @GET
    public Balance getBalanceByUserId(final Long userId) {
        return dbi.inTransaction((handle, status) -> {
            BalanceRepository repository = handle.attach(BalanceRepository.class);
            return repository.findBalanceByUserId(userId);
        });
    }

    @POST
    public Result addBalance(@Body final Balance balanceDto) {
        return dbi.inTransaction((handle, status) -> {
            BalanceRepository repository = handle.attach(BalanceRepository.class);
            repository.addBalance(balanceDto);
            return Results.with(Status.CREATED);
        });
    }
}
