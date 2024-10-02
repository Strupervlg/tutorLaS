package com.api.serverLaS.repositories;

import com.api.serverLaS.models.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public class UserRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<User> getList() {
        return jdbcTemplate.query("SELECT * FROM users",
                (resultSet, rowNum) -> new User(resultSet.getString("uid"), resultSet.getString("fio"), resultSet.getString("group_name")));
    }

    public User getById(String uid) {
        return jdbcTemplate.queryForObject("SELECT * FROM users where uid = ?",
                new Object[]{uid},
                (resultSet, rowNum) -> new User(resultSet.getString("uid"), resultSet.getString("fio"), resultSet.getString("group_name")));
    }

    public User create(String fio, String groupName) {
        String uid = UUID.randomUUID().toString();
        String sql = "INSERT INTO users (uid, fio, group_name) " +
                "VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{uid, fio, groupName});

        return getById(uid);
    }
}
