package com.alfardil.ghostwriter.common.service.telegram;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TelegramService {

  private static final Logger log = LoggerFactory.getLogger(
    TelegramService.class
  );
  private static final String PARSE_MODE = "MarkdownV2";

  private final RestClient restClient;

  public TelegramService(RestClient telegramClient) {
    this.restClient = telegramClient;
  }

  public void sendMessage(String userId, String unsafeText) {
    try {
      String text =
        unsafeText.length() >= 4000
          ? unsafeText.substring(0, 4000) + "...(truncated)"
          : unsafeText;

      Map<String, Object> body = Map.of(
        "chat_id",
        userId,
        "text",
        text,
        "parse_mode",
        PARSE_MODE
      );

      restClient
        .post()
        .uri("/sendMessage")
        .contentType(MediaType.APPLICATION_JSON)
        .body(body)
        .retrieve()
        .toBodilessEntity();

      log.info("Telegram reply sent!");
    } catch (Exception e) {
      log.error(
        "Telegram reply send failed with userId={} | error={}",
        userId,
        e.getMessage()
      );
    }
  }
}
