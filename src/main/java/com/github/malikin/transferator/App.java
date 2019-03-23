package com.github.malikin.transferator;

import com.github.malikin.transferator.dao.AccountRepository;
import com.github.malikin.transferator.dao.BalanceRepository;
import com.github.malikin.transferator.dto.Account;
import com.github.malikin.transferator.dto.Balance;
import com.github.malikin.transferator.rest.AccountController;
import com.github.malikin.transferator.rest.TransactionController;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.sqlobject.SqlObjectPlugin;
import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;
import org.jooby.jdbi.Jdbi3;
import org.jooby.json.Jackson;

public class App extends Jooby {

    {
        use(new Jackson());

        use(new Jdbc("db"));

        use(new Jdbi3().doWith((jdbi, config) -> {
                    jdbi.installPlugin(new SqlObjectPlugin());
                    jdbi.useHandle(h -> h.execute(config.getString("schema")));
                })
        );

        // Create bank account with a lot of money :)
        onStart(() -> {
            Jdbi jdbi = require(Jdbi.class);
            jdbi.useHandle(h -> {
                AccountRepository accountRepository = h.attach(AccountRepository.class);
                accountRepository.addAccount(new Account(1L, "Bank"));

                BalanceRepository balanceRepository = h.attach(BalanceRepository.class);
                balanceRepository.addBalance(new Balance(1L, 1_000_000D));
            });
        });

        use(AccountController.class);
        use(TransactionController.class);
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }
}
