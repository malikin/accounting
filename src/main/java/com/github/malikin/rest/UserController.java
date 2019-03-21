package com.github.malikin.rest;

import com.github.malikin.dto.User;
import org.jooby.mvc.POST;
import org.jooby.mvc.Path;

@Path("/user")
public class UserController {

    @POST
    public void createUser(User userDto) {

    }
}
