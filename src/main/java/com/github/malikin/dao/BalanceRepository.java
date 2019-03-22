package com.github.malikin.dao;

import com.github.malikin.dto.Balance;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

public interface BalanceRepository {

    @SqlQuery("select user_id as userId, amount from balance where user_id = :id")
    @MapResultAsBean
    Balance findBalanceByUserId(@Bind("id") Long id);

    @SqlUpdate("insert into balance (user_id, amount) values (:balance.userId, :balance.amount)")
    void addBalance(@BindBean("balance") Balance balance);
}
