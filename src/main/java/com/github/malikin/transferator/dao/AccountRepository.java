package com.github.malikin.transferator.dao;

import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.mapper.AccountMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.List;

@RegisterRowMapper(AccountMapper.class)
public interface AccountRepository {

    @SqlQuery("select * from account")
    List<Account> findAll();

    @SqlQuery("select * from account where id = :id")
    Account findAccountById(@Bind("id") Long id);

    @SqlUpdate("insert into account (name) values (:account.name)")
    @GetGeneratedKeys
    Long addAccount(@BindBean("account") Account account);
}
