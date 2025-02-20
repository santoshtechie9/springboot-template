package com.example.msk;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class KafkaTopicCreator implements CommandLineRunner {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${app.kafka.topic.name}")
    private String topicName;

    public static void main(String[] args) {
        SpringApplication.run(KafkaTopicCreator.class, args);
    }

    @Override
    public void run(String... args) throws ExecutionException, InterruptedException {
        Properties config = new Properties();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);

        try (AdminClient adminClient = AdminClient.create(config)) {
            Set<String> existingTopics = adminClient.listTopics().names().get();
            
            if (existingTopics.contains(topicName)) {
                System.out.println("✅ Topic already exists: " + topicName);
                return;
            }

            NewTopic topic = new NewTopic(topicName, 1, (short) 1);
            adminClient.createTopics(Collections.singletonList(topic)).all().get();
            System.out.println("🎉 Kafka Topic Created: " + topicName);
        } catch (Exception e) {
            System.err.println("⚠️ Error Creating Topic: " + e.getMessage());
        }
    }
}
