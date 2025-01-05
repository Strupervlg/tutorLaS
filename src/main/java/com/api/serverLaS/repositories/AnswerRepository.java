package com.api.serverLaS.repositories;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class AnswerRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void create(int solutionId, String resultText, String value, String answer) {
        String sql = "INSERT INTO answers (solution_id, result_text, value, answer) " +
                "VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(sql, new Object[]{solutionId, resultText, value, answer});
    }
}
