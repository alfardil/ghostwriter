package com.alfardil.ghostwriter.common.db.repos.task;

import com.alfardil.ghostwriter.common.db.models.Task.Task;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;

public class TaskSqlRepository implements TaskRepository {

    private final JdbcClient client;

    public TaskSqlRepository(final JdbcClient client) {
        this.client = client;
    }

    @Override
    public void createTask(final Task task) {
        task.setId(UUID.randomUUID().toString());

        task.setCreatedAt(
            client
                .sql(
                    """
                    INSERT INTO "Task" (id, "telegramId", prompt, status)
                    VALUES (:id, :tId, :status)
                    RETURNING "createdAt"
                    """
                )
                .param("id", task.getId())
                .param("tId", task.getTelegramId())
                .param("status", task.getStatus().name())
                .query((rs, rowNum) ->
                    rs.getObject("createdAt", OffsetDateTime.class)
                )
                .single()
        );
    }

    @Override
    public Task getTaskById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
            "Unimplemented method 'getTaskById'"
        );
    }

    @Override
    public boolean updateTask(Task task) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
            "Unimplemented method 'updateTask'"
        );
    }

    @Override
    public boolean deleteTaskById(String id) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException(
            "Unimplemented method 'deleteTaskById'"
        );
    }
}
