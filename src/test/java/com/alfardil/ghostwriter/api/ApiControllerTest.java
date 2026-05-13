package com.alfardil.ghostwriter.api;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alfardil.ghostwriter.kafka.producer.KafkaProducerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiController.class)
@TestPropertySource(properties = "app.telegram.bot-token=test-token")
class ApiControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockitoBean
  private KafkaProducerService kafkaProducerService;

  @Test
  @DisplayName("Valid message is forwarded to Kafka")
  void validMessage() throws Exception {
    String json =
      """
      {
        "update_id": 1,
        "message": {
          "message_id": 1,
          "text": "hello",
          "from": {"id": 123, "username": "testuser"}
        }
      }
      """;

    mockMvc
      .perform(
        post("/api/webhook").contentType(MediaType.APPLICATION_JSON).content(json)
      )
      .andExpect(status().isOk());

    verify(kafkaProducerService).sendMessage("123", "hello");
  }

  @Test
  @DisplayName("Missing message field returns 200 without Kafka call")
  void missingMessage() throws Exception {
    String json = "{\"update_id\": 1}";

    mockMvc
      .perform(
        post("/api/webhook").contentType(MediaType.APPLICATION_JSON).content(json)
      )
      .andExpect(status().isOk());

    verifyNoInteractions(kafkaProducerService);
  }

  @Test
  @DisplayName("Missing text field returns 200 without Kafka call")
  void missingText() throws Exception {
    String json =
      """
      {
        "update_id": 1,
        "message": {
          "message_id": 1,
          "from": {"id": 123}
        }
      }
      """;

    mockMvc
      .perform(
        post("/api/webhook").contentType(MediaType.APPLICATION_JSON).content(json)
      )
      .andExpect(status().isOk());

    verifyNoInteractions(kafkaProducerService);
  }
}
