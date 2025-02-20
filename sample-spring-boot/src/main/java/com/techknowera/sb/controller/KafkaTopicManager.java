package com.example.msk;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.kafka.KafkaClient;
import software.amazon.awssdk.services.kafka.model.GetBootstrapBrokersRequest;
import software.amazon.awssdk.services.kafka.model.GetBootstrapBrokersResponse;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@SpringBootApplication
public class MSKTopicManager {

    private static final Logger logger = LoggerFactory.getLogger(MSKTopicManager.class);
    private static final String TOPIC_NAME = "your-topic-name"; // Change this to your desired topic name
    private static final String AWS_REGION = "us-east-1"; // Change to your AWS region
    private static final String CLUSTER_ARN = "arn:aws:kafka:us-east-1:your-account-id:cluster/your-cluster-name"; // Change to your cluster ARN

    public static void main(String[] args) {
        SpringApplication.run(MSKTopicManager.class, args);
    }

    @Bean
    public CommandLineRunner run() {
        return args -> {
            // Step 1: Initialize AWS Kafka Client with IAM authentication
            AwsBasicCredentials awsCredentials = AwsBasicCredentials.create("your-access-key-id", "your-secret-access-key");

            KafkaClient kafkaClient = KafkaClient.builder()
                    .credentialsProvider(StaticCredentialsProvider.create(awsCredentials))
                    .region(Region.of(AWS_REGION))
                    .build();

            // Step 2: Get Bootstrap Brokers from AWS MSK
            String bootstrapBrokers = getBootstrapBrokers(kafkaClient);
            if (bootstrapBrokers == null) {
                logger.error("❌ Failed to retrieve bootstrap brokers. Exiting.");
                return;
            }

            // Step 3: Set up Kafka Admin Client
            Properties config = new Properties();
            config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapBrokers);
            try (AdminClient adminClient = AdminClient.create(config)) {

                // Step 4: Check if the topic exists
                if (!topicExists(adminClient, TOPIC_NAME)) {
                    logger.info("✅ Topic '{}' does not exist. Creating it now...", TOPIC_NAME);
                    createTopic(adminClient, TOPIC_NAME, 3, (short) 1);
                } else {
                    logger.info("✔️ Topic '{}' already exists.", TOPIC_NAME);
                }

                // Step 5: List all topics
                listTopics(adminClient);
            }
        };
    }

    /**
     * Retrieves the bootstrap brokers for the AWS MSK cluster.
     */
    private String getBootstrapBrokers(KafkaClient kafkaClient) {
        try {
            GetBootstrapBrokersRequest request = GetBootstrapBrokersRequest.builder()
                    .clusterArn(CLUSTER_ARN)
                    .build();
            GetBootstrapBrokersResponse response = kafkaClient.getBootstrapBrokers(request);
            String brokers = response.bootstrapBrokerString();
            logger.info("🟢 Bootstrap Brokers: {}", brokers);
            return brokers;
        } catch (Exception e) {
            logger.error("⚠️ Error retrieving bootstrap brokers: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Checks if a given topic exists in the Kafka cluster.
     */
    private boolean topicExists(AdminClient adminClient, String topicName) {
        try {
            KafkaFuture<Set<String>> topicsFuture = adminClient.listTopics().names();
            Set<String> topics = topicsFuture.get();
            return topics.contains(topicName);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("⚠️ Error checking topic existence: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Creates a Kafka topic with the specified name, partitions, and replication factor.
     */
    private void createTopic(AdminClient adminClient, String topicName, int partitions, short replicationFactor) {
        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor);
        try {
            adminClient.createTopics(Collections.singletonList(newTopic)).all().get();
            logger.info("✅ Successfully created topic: {}", topicName);
        } catch (Exception e) {
            logger.error("⚠️ Error creating topic '{}': {}", topicName, e.getMessage());
        }
    }

    /**
     * Lists all topics in the Kafka cluster.
     */
    private void listTopics(AdminClient adminClient) {
        try {
            KafkaFuture<Set<String>> topicsFuture = adminClient.listTopics().names();
            Set<String> topics = topicsFuture.get();
            logger.info("📜 Available topics: {}", topics);
        } catch (InterruptedException | ExecutionException e) {
            logger.error("⚠️ Error listing topics: {}", e.getMessage());
        }
    }
}
