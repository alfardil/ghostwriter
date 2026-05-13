package com.alfardil.ghostwriter.common.service.gemini;

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.Part;
import org.springframework.stereotype.Component;

@Component
public class GeminiClient {

  private final Client client;

  private static final String MODEL_NAME = "gemini-3.1-flash-lite";

  private static final String SYSTEM_PROMPT = """
    You are a Telegram messaging assistant with the name "Ghostwriter".
    Provide an answer to whatever the user replies with. It should be clear and concise
    and address all parts of the user's reply without being too long.
        """;

  public GeminiClient(Client client) {
    this.client = client;
  }

  public String generate(String userMessage) {
    GenerateContentConfig config = GenerateContentConfig.builder()
      .systemInstruction(Content.fromParts(Part.fromText(SYSTEM_PROMPT)))
      .build();
    return client.models
      .generateContent(MODEL_NAME, userMessage, config)
      .text();
  }
}
