package com.alfardil.ghostwriter.common.db.models.Task;

import com.alfardil.ghostwriter.common.db.helper.annotations.NotNullColumn;
import java.time.OffsetDateTime;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder
@ToString
@EqualsAndHashCode
public class Task {

    @NotNullColumn
    private String id;

    @NotNullColumn
    private String telegramId;

    @NotNullColumn
    private String prompt;

    @NotNullColumn
    private TaskStatus status;

    @NotNullColumn
    private OffsetDateTime createdAt;
}
