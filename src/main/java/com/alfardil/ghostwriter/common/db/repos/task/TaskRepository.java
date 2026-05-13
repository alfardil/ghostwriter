package com.alfardil.ghostwriter.common.db.repos.task;

import com.alfardil.ghostwriter.common.db.models.task.Task;

public interface TaskRepository {
    void createTask(Task task);

    Task getTaskById(String id);

    boolean updateTask(Task task);

    boolean deleteTaskById(String id);
}
