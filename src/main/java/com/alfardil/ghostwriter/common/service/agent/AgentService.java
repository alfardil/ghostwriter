package com.alfardil.ghostwriter.common.service.agent;

import com.alfardil.ghostwriter.common.db.models.message.Message;
import com.alfardil.ghostwriter.common.db.repos.message.MessageRepository;
import com.alfardil.ghostwriter.common.service.gemini.GeminiClient;
import com.alfardil.ghostwriter.kafka.producer.KafkaProducerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AgentService {

  private final GeminiClient geminiClient;
  private final MessageRepository messageRepository;
  private final KafkaProducerService kafkaProducerService;

  public AgentService(
    final GeminiClient geminiClient,
    final MessageRepository messageRepository,
    final KafkaProducerService kafkaProducerService
  ) {
    this.geminiClient = geminiClient;
    this.messageRepository = messageRepository;
    this.kafkaProducerService = kafkaProducerService;
  }

  @KafkaListener(topics = "task", groupId = "ghostwriter-agent-consumer-group")
  public void consume(ConsumerRecord<String, String> record) {
    String userId = record.key();
    String userMessage = record.value();

    if (userMessage == null || userMessage.trim().isEmpty()) {
      log.warn("Received an empty message");
      return;
    }

    try {
      String aiResponse = geminiClient.generate(userMessage);

      Message message = Message.builder()
        .telegramId(userId)
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
