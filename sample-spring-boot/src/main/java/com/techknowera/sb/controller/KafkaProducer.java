package com.example.msk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaProducerService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerService.class);
    private final KafkaTemplate<String, String> kafkaTemplate;

    public KafkaProducerService(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(String message) {
        LOGGER.info("🚀 Sending message: {}", message);

        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send("msk-test-topic", message);

        future.thenAccept(result -> LOGGER.info("✅ Sent message to partition {} at offset {}",
                result.getRecordMetadata().partition(), result.getRecordMetadata().offset()))
              .exceptionally(ex -> {
                  LOGGER.error("⚠️ Failed to send message: {}", ex.getMessage());
                  return null;
              });
    }
}
