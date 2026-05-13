package com.alfardil.ghostwriter.common.db.repos.message;

import com.alfardil.ghostwriter.common.db.models.message.Message;
import java.time.OffsetDateTime;
import java.util.UUID;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;

@Component
public class MessageSqlRepository implements MessageRepository {

  private final JdbcClient client;

  public MessageSqlRepository(final JdbcClient client) {
    this.client = client;
  }

  @Override
  public void createMessage(Message message) {
    UUID id = UUID.randomUUID();
    message.setId(id.toString());

    message.setCreatedAt(
      client
        .sql(
          """
          INSERT INTO "Message" (id, "userMessage", "aiResponse")
          VALUES (:id, :userMessage, :aiResponse)
          RETURNING "createdAt"
          """
        )
        .param("id", id)
        .param("userMessage", message.getUserMessage())
        .param("aiResponse", message.getAiResponse())
        .query((rs, rowNum) -> rs.getObject("createdAt", OffsetDateTime.class))
        .single()
    );
  }

  @Override
  public Message getMessageById(String id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
      "Unimplemented method 'getMessageById'"
    );
  }

  @Override
  public boolean updateMessage(Message message) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
      "Unimplemented method 'updateMessage'"
    );
  }

  @Override
  public boolean deleteMessageById(String id) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException(
      "Unimplemented method 'deleteMessageById'"
    );
  }
}
