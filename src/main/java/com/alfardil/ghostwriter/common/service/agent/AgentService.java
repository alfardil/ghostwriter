package com.alfardil.ghostwriter.common.service.agent;

import com.alfardil.ghostwriter.common.db.models.message.Message;
import com.alfardil.ghostwriter.common.db.repos.message.MessageRepository;
import com.alfardil.ghostwriter.kafka.producer.KafkaProducerService;
import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.Part;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService {

  private final Client client;
  private final String MODEL_NAME = "gemini-3.1-flash-lite"; // change while iterating
  private final String SYSTEM_PROMPT = """
    You are a messaging assistant. Provide a concise answer to whatever the user replies with.
        """;

  private final MessageRepository messageRepository;
  private final KafkaProducerService kafkaProducerService;

  public AgentService(
    final Client client,
    final MessageRepository messageRepository,
    final KafkaProducerService kafkaProducerService
  ) {
    this.client = client;
    this.messageRepository = messageRepository;
    this.kafkaProducerService = kafkaProducerService;
  }

  @KafkaListener(topics = "task", groupId = "ghostwriter-agent-consumer-group")
  public void consume(ConsumerRecord<String, String> record) {
    String userId = record.key();
    String userMessage = record.value();

    if (userMessage == null || userMessage.trim().isEmpty()) {
      log.warn("Recieved an empty message");
      return;
    }

    try {
      GenerateContentConfig config = GenerateContentConfig.builder()
        .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_PROMPT)))
        .build();

      GenerateContentResponse response = client.models.generateContent(
        MODEL_NAME,
        userMessage,
        config
      );

      String aiResponse = response.text();

      Message message = Message.builder()
        .userMessage(userMessage)
        .aiResponse(aiResponse)
        .build();
      messageRepository.createMessage(message);

      // send AI reply into the queue so we don't have to keep retrying and run out of api credits
      kafkaProducerService.sendReply(userId, aiResponse);
    } catch (Exception e) {
      log.error(
        "Failed to process message for userId={}: {}",
        userId,
        e.getMessage(),
        e
      );
      throw e;
    }
  }
}
