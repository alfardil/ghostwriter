/**
Merely for testing kafka.

package com.alfardil.ghostwriter.kafka.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TaskProducer implements CommandLineRunner {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(5000);
        for (int i = 0; i < 100; i++) {
            String message =
                "Testing if this shows up on the console " + (i + 1) + "!";
            kafkaTemplate.send("task", message).get();
            log.info("Message sent: {}", message);
        }
    }
}
*/
