package com.github.malikin.dao;

import com.github.malikin.dto.User;
import org.skife.jdbi.v2.sqlobject.Bind;
import org.skife.jdbi.v2.sqlobject.BindBean;
import org.skife.jdbi.v2.sqlobject.GetGeneratedKeys;
import org.skife.jdbi.v2.sqlobject.SqlQuery;
import org.skife.jdbi.v2.sqlobject.SqlUpdate;
import org.skife.jdbi.v2.sqlobject.helpers.MapResultAsBean;

import java.io.Closeable;
import java.util.List;

public interface UserRepository extends Closeable {

    @SqlQuery("select * from user")
    @MapResultAsBean
    List<User> findAll();

    @SqlQuery("select * from user where id = :id")
    @MapResultAsBean
    User findUserById(@Bind("id") Long id);

    @SqlUpdate("insert into user (name) values (:user.name)")
    @GetGeneratedKeys
    Long addUser(@BindBean("user") User user);
}
