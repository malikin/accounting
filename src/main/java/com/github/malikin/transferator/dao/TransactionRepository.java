package com.github.malikin.transferator.dao;

import com.github.malikin.transferator.dto.Transaction;
import com.github.malikin.transferator.mapper.TransactionMapper;
import org.jdbi.v3.sqlobject.config.RegisterRowMapper;
import org.jdbi.v3.sqlobject.customizer.Bind;
import org.jdbi.v3.sqlobject.customizer.BindBean;
import org.jdbi.v3.sqlobject.statement.SqlQuery;
import org.jdbi.v3.sqlobject.statement.SqlUpdate;

import java.util.Set;

@RegisterRowMapper(TransactionMapper.class)
public interface TransactionRepository {

    @SqlQuery("select * from transaction where operation_uuid = :operationUuid")
    Set<Transaction> findTransactionsByOperationUuid(@Bind("operationUuid") String operationUuid);

    @SqlQuery("select * from transaction where sender_id = :id or recipient_id = :id")
    Set<Transaction> findTransactionsByAccountId(@Bind("id") Long id);

    @SqlUpdate("insert into transaction (operation_uuid, sender_id, recipient_id, timestamp, amount) " +
            "values (:transaction.operationUuid, :transaction.senderId, :transaction.recipientId, :transaction.timestamp, :transaction.amount)")
    void addTransaction(@BindBean("transaction") Transaction transaction);
}
