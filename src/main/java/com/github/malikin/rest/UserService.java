package com.github.malikin.rest;

import javax.inject.Inject;
import javax.sql.DataSource;

public class UserService {

    private final DataSource dataSource;

    @Inject
    public UserService(final DataSource dataSource) {
        this.dataSource = dataSource;
    }
}
