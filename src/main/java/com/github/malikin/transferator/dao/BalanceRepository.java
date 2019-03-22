package com.github.malikin.transferator.dao;

import com.github.malikin.transferator.dto.Balance;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

public interface BalanceRepository {

    @SqlQuery("select account_id as accountId, amount from balance where account_id = :id")
    @MapResultAsBean
    Balance findBalanceByAccountId(@Bind("id") Long id);

    @SqlUpdate("insert into balance (account_id, amount) values (:balance.accountId, :balance.amount)")
    void addBalance(@BindBean("balance") Balance balance);
}
