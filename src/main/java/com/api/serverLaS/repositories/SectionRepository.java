package com.api.serverLaS.repositories;

import com.api.serverLaS.models.Section;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SectionRepository {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TaskRepository taskRepository;

    public Section getById(int id) {
        return jdbcTemplate.queryForObject("SELECT * FROM sections where id = ?",
                new Object[]{id},
                (resultSet, rowNum) -> new Section(resultSet.getString("name"), taskRepository.getListBySectionId(resultSet.getInt("id"))));
    }

    public Section getByName(String name) {
        return jdbcTemplate.queryForObject("SELECT * FROM sections where name = ?",
                new Object[]{name},
                (resultSet, rowNum) -> new Section(resultSet.getString("name"), taskRepository.getListBySectionId(resultSet.getInt("id"))));
    }
}
