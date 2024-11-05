package com.api.serverLaS.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public void createTables() {
        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS users" +
                "(uid CHAR(36) default (lower(hex(randomblob(4))) || '-' || lower(hex(randomblob(2))) || '-4' || substr(lower(hex(randomblob(2))),2) || '-' || substr('89ab',abs(random()) % 4 + 1, 1) || substr(lower(hex(randomblob(2))),2) || '-' || lower(hex(randomblob(6)))), " +
                "fio VARCHAR(256), group_name VARCHAR(256))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS sections" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name CHAR(50))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS tasks" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name CHAR(256)," +
                "name_ttl CHAR(256)," +
                "name_json CHAR(256)," +
                "section_id INTEGER," +
                "CONSTRAINT tasks_sections_fk FOREIGN KEY (section_id) REFERENCES sections (id))");

        jdbcTemplate.execute("CREATE TABLE IF NOT EXISTS solutions" +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_uid CHAR(36)," +
                "task_id INTEGER, " +
                "count_of_correct INTEGER, " +
                "count_of_mistakes INTEGER, " +
                "count_of_hints INTEGER," +
                "CONSTRAINT solutions_users_fk FOREIGN KEY (user_uid) REFERENCES users (uid)" +
                "CONSTRAINT solutions_tasks_fk FOREIGN KEY (task_id) REFERENCES tasks (id))");
    }

    public void fillDatabase() {
        jdbcTemplate.execute("DELETE FROM sections");
        jdbcTemplate.execute("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = 'sections'");
        jdbcTemplate.execute("DELETE FROM tasks");
        jdbcTemplate.execute("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = 'tasks'");
        jdbcTemplate.execute("DELETE FROM users");
        jdbcTemplate.execute("UPDATE SQLITE_SEQUENCE SET seq = 0 WHERE name = 'users'");

        jdbcTemplate.execute("INSERT INTO sections (name) VALUES ('Lifetime')");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task1', '11.ttl', '1.json', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task2', '21.ttl', '2.json', 1)");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task3', '31.ttl', '3.json', 1)");

        jdbcTemplate.execute("INSERT INTO sections (name) VALUES ('Scope')");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task21', '11.ttl', '1.json', 2)");

        jdbcTemplate.execute("INSERT INTO sections (name) VALUES ('Expression')");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task31', '1.ttl', '1.json', 3)");
        jdbcTemplate.execute("INSERT INTO tasks (name, name_ttl, name_json, section_id) VALUES ('task32', '2.ttl', '2.json', 3)");





        jdbcTemplate.execute("INSERT INTO users (fio, group_name, uid) VALUES ('test fio', 'tset group', '09a8f9ac-a4d1-41bb-9f32-aa957dd22f71')");
//        jdbcTemplate.execute("INSERT INTO solutions (user_uid, task_id, count_of_correct, count_of_mistakes, count_of_hints) VALUES ('7adc3590-1058-4a14-aa87-c625a229763c', 1, 0, 0, 0)");
    }
}
