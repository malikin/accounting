package com.github.malikin.transferator.mapper;

import com.github.malikin.transferator.dto.Account;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class AccountMapper implements RowMapper<Account> {

    @Override
    public Account map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Account(rs.getLong("id"), rs.getString("name"));
    }
}
