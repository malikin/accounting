package com.github.malikin.dao;

import com.github.malikin.dto.Transaction;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.util.Set;

public interface TransactionRepository {

    @SqlQuery("select id, operation_uuid as operationUuid, sender_id as senderId, recipient_id as recipientId, timestamp, amount " +
                    "from balance where id = :id")
    @MapResultAsBean
    Transaction findTransactionById(@Bind("id") Long id);

    @SqlQuery("select id, operation_uuid as operationUuid, sender_id as senderId, recipient_id as recipientId, timestamp, amount " +
            "from balance where sender_id = :id or recipient_id = :id")
    @MapResultAsBean
    Set<Transaction> findTransactionsByAccountId(@Bind("id") Long id);

    @SqlUpdate("insert into transaction (operation_uuid, sender_id, recipient_id, timestamp, amount) " +
            "values (:transaction.operationUuid, :transaction.senderId, :transaction.recipientId, :transaction.timestamp, :transaction.amount)")
    void addTransaction(@BindBean("transaction") Transaction transaction);
}
