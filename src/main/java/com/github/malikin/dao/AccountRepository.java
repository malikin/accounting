package com.github.malikin.dao;

import com.github.malikin.dto.Account;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.List;

public interface AccountRepository {

    @SqlQuery("select * from account")
    @MapResultAsBean
    List<Account> findAll();

    @SqlQuery("select * from account where id = :id")
    @MapResultAsBean
    Account findAccountById(@Bind("id") Long id);

    @SqlUpdate("insert into account (name) values (:account.name)")
    @GetGeneratedKeys
    Long addAccount(@BindBean("account") Account account);
}
