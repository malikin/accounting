package com.github.malikin.transferator.service;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dao.TransactionRepository;
import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.dto.Transaction;
import com.google.inject.Inject;
import org.jdbi.v3.core.Jdbi;
import org.jooby.Err;
import org.jooby.Status;

import java.util.Set;

public class AccountService {

    private final Jdbi jdbi;

    @Inject
    public AccountService(final Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public Account findAccountById(final Long id) {
        return jdbi.withHandle(handle -> {
            final AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAccountById(id);
        });
    }

    public Set<Account> findAllAccounts() {
        return jdbi.withHandle(handle -> {
            final AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAll();
        });
    }

    public Account findAccountByName(final String name) {
        return jdbi.withHandle(handle -> {
            final AccountRepository repository = handle.attach(AccountRepository.class);
            return repository.findAccountByName(name);
        });
    }

    public Set<Transaction> findTransactionsByAccountId(final Long accountId) {
        return jdbi.withHandle(handle -> {
            final TransactionRepository repository = handle.attach(TransactionRepository.class);
            return repository.findTransactionsByAccountId(accountId);
        });
    }

    public Account createAccount(final Account account) {
        return jdbi.inTransaction(handle -> {
            final AccountRepository accountRepository = handle.attach(AccountRepository.class);
            final BalanceRepository balanceRepository = handle.attach(BalanceRepository.class);

            final Account existedAccount = accountRepository.findAccountByName(account.getName());

            if (existedAccount != null) {
                throw new Err(Status.BAD_REQUEST, String.format("Account with name %s already exist", account.getName()));
            }

            final Long id = accountRepository.addAccount(account);
            balanceRepository.addBalance(new Balance(id, 0.0));

            return accountRepository.findAccountById(id);
        });
    }

    public Balance getBalanceByAccountId(final Long accountId) {
        return jdbi.withHandle(handle -> {
            final BalanceRepository repository = handle.attach(BalanceRepository.class);
            return repository.findBalanceByAccountId(accountId);
        });
    }
}
