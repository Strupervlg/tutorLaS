package com.api.serverLaS.repositories;

import com.api.serverLaS.models.Solution;
import org.apache.jena.base.Sys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class SolutionRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    public boolean hasSolution(String uid, int taskId) {
        String sql = "SELECT COUNT(1) FROM solutions where user_uid = ? and task_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, new Object[]{uid, taskId});
        return count != null && count == 1;
    }

    public Solution getByUserIdAndTaskId(String uid, int taskId) {
        return jdbcTemplate.queryForObject("SELECT * FROM solutions where user_uid = ? and task_id = ?",
                new Object[]{uid, taskId},
                (resultSet, rowNum) -> new Solution(userRepository.getById(uid), taskRepository.getById(taskId), resultSet.getInt("count_of_correct"), resultSet.getInt("count_of_mistakes"), resultSet.getInt("count_of_hints")));
    }

    public void create(String uid, int taskId) {
        String sql = "INSERT INTO solutions (user_uid, task_id, count_of_correct, count_of_mistakes, count_of_hints) " +
                "VALUES (?, ?, 0, 0, 0)";
        jdbcTemplate.update(sql, new Object[]{uid, taskId});
    }

    public void addCountOfCorrect(String uid, int taskId) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        this.updateCountOfCorrect(uid, taskId, solution.getCountOfCorrect() + 1);
    }

    public void addCountOfMistakes(String uid, int taskId) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        this.updateCountOfMistakes(uid, taskId, solution.getCountOfMistakes() + 1);
    }

    public void addCountOfHints(String uid, int taskId) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        this.updateCountOfHints(uid, taskId, solution.getCountOfHints() + 1);
    }

    public void updateCountOfCorrect(String uid, int taskId, int countOfCorrect) {
        String sql = "UPDATE solutions SET count_of_correct = ? WHERE user_uid = ? and task_id = ?";
        jdbcTemplate.update(sql, new Object[]{countOfCorrect, uid, taskId});
    }

    public void updateCountOfMistakes(String uid, int taskId, int countOfMistakes) {
        String sql = "UPDATE solutions SET count_of_mistakes = ? WHERE user_uid = ? and task_id = ?";
        jdbcTemplate.update(sql, new Object[]{countOfMistakes, uid, taskId});
    }

    public void updateCountOfHints(String uid, int taskId, int countOfHints) {
        String sql = "UPDATE solutions SET count_of_hints = ? WHERE user_uid = ? and task_id = ?";
        jdbcTemplate.update(sql, new Object[]{countOfHints, uid, taskId});
    }
}
