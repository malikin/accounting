package com.github.malikin.transferator;

import com.github.malikin.transferator.rest.BalanceController;
import com.github.malikin.transferator.rest.AccountController;
import com.github.malikin.transferator.rest.TransactionController;
import com.typesafe.config.Config;
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
                    jdbi.useHandle(h -> {
                        h.execute(config.getString("schema"));
                    });
                })
        );

        use(AccountController.class);
        use(BalanceController.class);
        use(TransactionController.class);
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }
}
