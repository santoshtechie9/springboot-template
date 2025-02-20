package com.example.msk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class KafkaConsumerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaConsumerService.class);

    @KafkaListener(topics = "msk-test-topic", groupId = "msk-group")
    public void listen(String message) {
        LOGGER.info("📩 Received Message: {}", message);
    }
}
