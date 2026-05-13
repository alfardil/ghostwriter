package com.alfardil.ghostwriter.common.db.repos.task;

import com.alfardil.ghostwriter.common.db.models.task.Task;
import com.alfardil.ghostwriter.common.db.models.task.TaskStatus;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class TaskSqlRepository implements TaskRepository {

  private final JdbcClient client;

  public TaskSqlRepository(final JdbcClient client) {
    this.client = client;
  }

  private final RowMapper<Task> taskMapper = (rs, _) ->
    Task.builder()
      .id(rs.getString("id"))
      .telegramId(rs.getString("telegramId"))
      .prompt(rs.getString("prompt"))
      .status(rs.getObject("status", TaskStatus.class))
      .createdAt(rs.getObject("createdAt", OffsetDateTime.class))
      .build();

  @Override
  public void createTask(final Task task) {
    task.setId(UUID.randomUUID().toString());

    task.setCreatedAt(
      client
        .sql(
          """
          INSERT INTO "Task" (id, "telegramId", prompt, status)
          VALUES (:id, :tId, :prompt, :status)
          RETURNING "createdAt"
          """
        )
        .param("id", task.getId())
        .param("tId", task.getTelegramId())
        .param("prompt", task.getPrompt())
        .param("status", task.getStatus().name())
        .query((rs, rowNum) -> rs.getObject("createdAt", OffsetDateTime.class))
        .single()
    );
  }

  @Override
  public Task getTaskById(String id) {
    return client
      .sql(
        """
        SELECT id, "telegramId", prompt, status, "createdAt"
        FROM "Task"
        WHERE id = :id
        """
      )
      .param("id", id)
      .query(taskMapper)
      .optional()
      .orElse(null);
  }

  @Override
  public boolean updateTask(Task task) {
    String id = task.getId();
    int updatedRows = client
      .sql(
        """
        UPDATE "Task"
        SET
          "telegramId" = :tId,
          prompt = :prompt,
          status = :status
        WHERE id = :id
        """
      )
      .param("tId", task.getTelegramId())
      .param("prompt", task.getPrompt())
      .param("status", task.getStatus())
      .param("id", id)
      .update();

    return updatedRows > 0;
  }

  @Override
  public boolean deleteTaskById(String id) {
    int updatedRows = client
      .sql(
        """
        DELETE from "Task"
        WHERE id = :id
        """
      )
      .param("id", id)
      .update();

    return updatedRows > 0;
  }
}
