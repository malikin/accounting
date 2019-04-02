package com.github.malikin.transferator.mapper;

import com.github.malikin.transferator.dto.Transaction;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class TransactionMapper implements RowMapper<Transaction> {

    @Override
    public Transaction map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new Transaction(
                rs.getLong("id"),
                UUID.fromString(rs.getString("operation_uuid")),
                rs.getLong("sender_id"),
                rs.getLong("recipient_id"),
                rs.getBigDecimal("amount"),
                rs.getTimestamp("timestamp").toInstant()
        );
    }
}
