package com.github.malikin.rest;

import com.github.malikin.dao.UserRepository;
import com.github.malikin.dto.User;
import com.google.inject.Inject;
import org.jooby.mvc.Body;
import org.jooby.mvc.GET;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;
import org.skife.jdbi.v2.DBI;

import java.util.List;

@Path("/user")
public class UserController {

    private final DBI dbi;

    @Inject
    public UserController(final DBI dbi) {
        this.dbi = dbi;
    }

    @Path("/:id")
    @GET
    public User getUserById(final Long id) {
        return dbi.inTransaction((handle, status) -> {
            UserRepository repository = handle.attach(UserRepository.class);
            return repository.findUserById(id);
        });
    }

    @GET
    public List<User> getAllUsers() {
        return dbi.inTransaction((handle, status) -> {
            UserRepository repository = handle.attach(UserRepository.class);
            return repository.findAll();
        });
    }

    @POST
    public Long createUser(@Body final User userDto) {
        return dbi.inTransaction((handle, status) -> {
            UserRepository repository = handle.attach(UserRepository.class);
            return repository.addUser(userDto);
        });
    }
}
