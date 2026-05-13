package com.alfardil.ghostwriter.common.service.agent;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.alfardil.ghostwriter.common.db.models.message.Message;
import com.alfardil.ghostwriter.common.db.repos.message.MessageRepository;
import com.alfardil.ghostwriter.common.service.gemini.GeminiClient;
import com.alfardil.ghostwriter.kafka.producer.KafkaProducerService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgentServiceTest {

  @Mock
  private GeminiClient geminiClient;

  @Mock
  private MessageRepository messageRepository;

  @Mock
  private KafkaProducerService kafkaProducerService;

  @InjectMocks
  private AgentService agentService;

  @Test
  @DisplayName("Valid message calls AI, saves to DB, and sends reply")
  void validMessage() {
    when(geminiClient.generate("hello")).thenReturn("AI response");
    ConsumerRecord<String, String> record = new ConsumerRecord<>(
      "task",
      0,
      0L,
      "user123",
      "hello"
    );

    agentService.consume(record);

    verify(geminiClient).generate("hello");

    ArgumentCaptor<Message> captor = ArgumentCaptor.forClass(Message.class);
    verify(messageRepository).createMessage(captor.capture());
    Message saved = captor.getValue();
    assertThat(saved.getTelegramId()).isEqualTo("user123");
    assertThat(saved.getUserMessage()).isEqualTo("hello");
    assertThat(saved.getAiResponse()).isEqualTo("AI response");

    verify(kafkaProducerService).sendReply("user123", "AI response");
  }

  @Test
  @DisplayName("Null message is ignored")
  void nullMessage() {
    ConsumerRecord<String, String> record = new ConsumerRecord<>(
      "task",
      0,
      0L,
      "user123",
      null
    );

    agentService.consume(record);

    verifyNoInteractions(geminiClient, messageRepository, kafkaProducerService);
  }

  @Test
  @DisplayName("Blank message is ignored")
  void blankMessage() {
    ConsumerRecord<String, String> record = new ConsumerRecord<>(
      "task",
      0,
      0L,
      "user123",
      "   "
    );

    agentService.consume(record);

    verifyNoInteractions(geminiClient, messageRepository, kafkaProducerService);
  }

  @Test
  @DisplayName("AI exception is rethrown")
  void aiThrows() {
    RuntimeException ex = new RuntimeException("AI error");
    when(geminiClient.generate(any())).thenThrow(ex);
    ConsumerRecord<String, String> record = new ConsumerRecord<>(
      "task",
      0,
      0L,
      "user123",
      "hello"
    );

    assertThatThrownBy(() -> agentService.consume(record)).isSameAs(ex);

    verifyNoInteractions(messageRepository, kafkaProducerService);
  }

  @Test
  @DisplayName("DB exception is rethrown")
  void dbThrows() {
    when(geminiClient.generate(any())).thenReturn("AI response");
    doThrow(new RuntimeException("DB error")).when(messageRepository).createMessage(any());
    ConsumerRecord<String, String> record = new ConsumerRecord<>(
      "task",
      0,
      0L,
      "user123",
      "hello"
    );

    assertThatThrownBy(() -> agentService.consume(record)).hasMessage("DB error");

    verifyNoInteractions(kafkaProducerService);
  }
}
