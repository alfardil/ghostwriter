package com.alfardil.ghostwriter.common.db.repos.test;

import com.alfardil.ghostwriter.common.db.models.Task.TaskStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class DatabaseTestRunner implements CommandLineRunner {

    private final JdbcClient jdbcClient;

    public DatabaseTestRunner(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public void run(String... args) {
        System.out.println("testing db connection");

        try {
            UUID testId = UUID.randomUUID();
            String testTelegramId = "123456789";

            var createdAt = jdbcClient
                .sql(
                    """
                    INSERT INTO "Task" (id, "telegramId", "prompt", status)
                    VALUES (:id, :tId, :prompt, :status::taskStatus)
                    RETURNING "createdAt"
                    """
                )
                .param("id", testId)
                .param("tId", testTelegramId)
                .param("prompt", "testing")
                .param("status", TaskStatus.INCOMPLETE.name())
                .query(OffsetDateTime.class)
                .single();

            System.out.println("Task created at: " + createdAt);
        } catch (Exception e) {
            System.err.println("test failed");
            e.printStackTrace();
        }
    }
}
