package com.github.malikin.transferator.dao;

import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.mapper.BalanceMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

@RegisterRowMapper(BalanceMapper.class)
public interface BalanceRepository {

    @SqlQuery("select account_id, amount from balance where account_id = :id")
    Balance findBalanceByAccountId(@Bind("id") Long id);

    @SqlQuery("select account_id, amount from balance where account_id = :id for update")
    Balance findBalanceByAccountIdWithLock(@Bind("id") Long id);

    @SqlUpdate("insert into balance (account_id, amount) values (:balance.accountId, :balance.amount)")
    void addBalance(@BindBean("balance") Balance balance);

    @SqlUpdate("update balance set amount=:balance.amount where account_id = :balance.accountId")
    void updateBalance(@BindBean("balance") Balance balance);
}
