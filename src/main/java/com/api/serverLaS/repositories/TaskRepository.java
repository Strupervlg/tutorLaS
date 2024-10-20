package com.api.serverLaS.repositories;

import com.api.serverLaS.models.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TaskRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Task> getList() {
        return jdbcTemplate.query("SELECT * FROM tasks",
                (resultSet, rowNum) -> new Task(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("name_ttl"), resultSet.getString("name_json")));
    }

    public List<Task> getFreeList(int sectionId, String uid) {
        return jdbcTemplate.query("SELECT id, name, name_ttl, name_json, section_id FROM tasks " +
                        "WHERE section_id = ? EXCEPT SELECT tasks.id, tasks.name, tasks.name_ttl, tasks.name_json, tasks.section_id FROM tasks " +
                        "LEFT JOIN solutions ON tasks.id = solutions.task_id " +
                        "LEFT JOIN users ON solutions.user_uid = ?" +
                        "WHERE solutions.id is not null",
                new Object[]{sectionId, uid},
                (resultSet, rowNum) -> new Task(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("name_ttl"), resultSet.getString("name_json")));
    }

    public List<Task> getListBySectionId(int sectionId) {
        return jdbcTemplate.query("SELECT * FROM tasks where section_id = ?",
                new Object[]{sectionId},
                (resultSet, rowNum) -> new Task(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("name_ttl"), resultSet.getString("name_json")));
    }

    public Task getById(int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM tasks where id = ?",
                new Object[]{id},
                (resultSet, rowNum) -> new Task(resultSet.getInt("id"), resultSet.getString("name"), resultSet.getString("name_ttl"), resultSet.getString("name_json")));
    }
}