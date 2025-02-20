package com.example.msk;

import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;

import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class MSKTopicManager {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // AWS Credentials (Replace with your real credentials)
    private static final String AWS_ACCESS_KEY = "YOUR_ACCESS_KEY";
    private static final String AWS_SECRET_KEY = "YOUR_SECRET_KEY";

    private AdminClient createKafkaAdminClient() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put("security.protocol", "SASL_SSL");
        props.put("sasl.mechanism", "AWS_MSK_IAM");
        props.put("sasl.jaas.config", "software.amazon.msk.auth.iam.IAMLoginModule required;");
        props.put("sasl.client.callback.handler.class", "software.amazon.msk.auth.iam.IAMClientCallbackHandler");

        // Attach AWS Credentials Programmatically
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(AWS_ACCESS_KEY, AWS_SECRET_KEY);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(awsCreds);
        props.put("aws.credentials.provider", credentialsProvider);

        return AdminClient.create(props);
    }

    public boolean topicExists(String topicName) {
        try (AdminClient adminClient = createKafkaAdminClient()) {
            KafkaFuture<Set<String>> future = adminClient.listTopics().names();
            return future.get().contains(topicName);
        } catch (ExecutionException | InterruptedException e) {
            System.err.println("⚠️ Error checking topic existence: " + e.getMessage());
            return false;
        }
    }

    public void createTopicIfNotExists(String topicName, int partitions, short replicationFactor) {
        if (topicExists(topicName)) {
            System.out.println("✅ Topic '" + topicName + "' already exists.");
            return;
        }

        try (AdminClient adminClient = createKafkaAdminClient()) {
            NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor)
                    .configs(Map.of(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_DELETE));

            CreateTopicsResult result = adminClient.createTopics(Collections.singleton(newTopic));
            result.all().get();
            System.out.println("✅ Topic '" + topicName + "' created successfully.");
        } catch (Exception e) {
            System.err.println("⚠️ Failed to create topic: " + e.getMessage());
        }
    }
}
