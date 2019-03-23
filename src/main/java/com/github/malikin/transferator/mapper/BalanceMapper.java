package com.github.malikin.transferator.mapper;

import com.github.malikin.transferator.dto.Balance;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class BalanceMapper implements RowMapper<Balance> {

    @Override
    public Balance map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Balance(rs.getLong("account_id"), rs.getDouble("amount"));
    }
}
