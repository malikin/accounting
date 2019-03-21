package com.github.malikin;

import com.github.malikin.rest.UserController;
import org.jooby.Jooby;
import org.jooby.jdbc.Jdbc;

/**
 * @author jooby generator
 */
public class App extends Jooby {

  {
    get("/", () -> "Hello World!");
    use(new Jdbc("db"));
    use(UserController.class);
  }

  public static void main(final String[] args) {
    run(App::new, args);
  }
}
