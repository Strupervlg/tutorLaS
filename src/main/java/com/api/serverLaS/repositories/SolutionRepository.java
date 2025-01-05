package com.api.serverLaS.repositories;

import com.api.serverLaS.models.Solution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Repository
public class SolutionRepository {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private AnswerRepository answerRepository;

    public boolean hasSolution(String uid, int taskId) {
        String sql = "SELECT COUNT(1) FROM solutions where user_uid = ? and task_id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, new Object[]{uid, taskId});
        return count != null && count == 1;
    }

    public Solution getByUserIdAndTaskId(String uid, int taskId) {
        return jdbcTemplate.queryForObject("SELECT * FROM solutions where user_uid = ? and task_id = ?",
                new Object[]{uid, taskId},
                (resultSet, rowNum) -> new Solution(resultSet.getInt("id"), userRepository.getById(uid), taskRepository.getById(taskId), resultSet.getInt("count_of_correct"), resultSet.getInt("count_of_mistakes"), resultSet.getInt("count_of_hints")));
    }

    public void create(String uid, int taskId) {
        String sql = "INSERT INTO solutions (user_uid, task_id, count_of_correct, count_of_mistakes, count_of_hints, created_at, updated_at) " +
                "VALUES (?, ?, 0, 0, 0, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String current = now.format(formatter);
        jdbcTemplate.update(sql, new Object[]{uid, taskId, current, current});
    }

    public void addCountOfCorrect(String uid, int taskId, String answer, String correctText) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        answerRepository.create(solution.getId(), correctText, "correct", answer);
        this.updateCountOfCorrect(uid, taskId, solution.getCountOfCorrect() + 1);
    }

    public void addCountOfMistakes(String uid, int taskId, String answer, String errorText) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        answerRepository.create(solution.getId(), errorText, "error", answer);
        this.updateCountOfMistakes(uid, taskId, solution.getCountOfMistakes() + 1);
    }

    public void addCountOfHints(String uid, int taskId, String answer, String hintText) {
        Solution solution = this.getByUserIdAndTaskId(uid, taskId);
        answerRepository.create(solution.getId(), hintText, "hint", answer);
        this.updateCountOfHints(uid, taskId, solution.getCountOfHints() + 1);
    }

    public void updateCountOfCorrect(String uid, int taskId, int countOfCorrect) {
        String sql = "UPDATE solutions SET count_of_correct = ?, updated_at = ? WHERE user_uid = ? and task_id = ?";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String current = now.format(formatter);
        jdbcTemplate.update(sql, new Object[]{countOfCorrect, current, uid, taskId});
    }

    public void updateCountOfMistakes(String uid, int taskId, int countOfMistakes) {
        String sql = "UPDATE solutions SET count_of_mistakes = ?, updated_at = ? WHERE user_uid = ? and task_id = ?";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String current = now.format(formatter);
        jdbcTemplate.update(sql, new Object[]{countOfMistakes, current, uid, taskId});
    }

    public void updateCountOfHints(String uid, int taskId, int countOfHints) {
        String sql = "UPDATE solutions SET count_of_hints = ?, updated_at = ? WHERE user_uid = ? and task_id = ?";
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String current = now.format(formatter);
        jdbcTemplate.update(sql, new Object[]{countOfHints, current, uid, taskId});
    }
}
