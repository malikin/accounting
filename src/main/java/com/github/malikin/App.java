package com.github.malikin;

import com.github.malikin.rest.UserController;
import com.typesafe.config.Config;
import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;
import org.jooby.jdbi.Jdbi;
import org.jooby.json.Jackson;
import org.skife.jdbi.v2.DBI;
import org.skife.jdbi.v2.Handle;

public class App extends Jooby {

    {
        use(new Jackson());

        use(new Jdbc("db"));

        use(new Jdbi()
                .doWith(
                        (DBI dbi, Config conf) -> {
                            try (Handle handle = dbi.open()) {
                                handle.execute(conf.getString("schema"));
                            }
                        }
                )
        );

        use(UserController.class);
    }

    public static void main(final String[] args) {
        run(App::new, args);
    }
}
