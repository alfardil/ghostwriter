package com.alfardil.ghostwriter.common.db.models.task;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JobStatus")
public enum TaskStatus {
    INCOMPLETE,
    PROCESSING,
    COMPLETED,
}
